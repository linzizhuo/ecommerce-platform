package com.cloudmall.service.impl;

import com.cloudmall.entity.Category;
import com.cloudmall.entity.Product;
import com.cloudmall.entity.Sku;
import com.cloudmall.mapper.CategoryMapper;
import com.cloudmall.mapper.ProductMapper;
import com.cloudmall.mapper.SkuMapper;
import com.cloudmall.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 商品服务 — 两级缓存实现
 *
 * 缓存架构:
 *   L1: Caffeine本地缓存（JVM堆内存，纳秒级响应）
 *       - 容量: productCache 1000条, categoryCache 10条
 *       - TTL:  5分钟（热点商品）/ 30分钟（分类，几乎不变）
 *
 *   L2: Redis分布式缓存（网络IO，毫秒级响应）
 *       - TTL: 30分钟 + 随机0~5分钟（防缓存雪崩）
 *       - 多实例共享，一台更新后另一台也能读到
 *
 * 缓存策略: Cache-Aside（旁路缓存）
 *   读: 查L1 → 未命中查L2 → 未命中查DB → 回填L2+L1
 *   写: 更新DB → 删除L2 → 让下一次读回源
 *
 * 防三大缓存问题:
 *   穿透: 空对象也缓存（短TTL），不再反复打库
 *   击穿: Redisson分布式锁互斥重建热点缓存
 *   雪崩: Redis TTL加随机值，避免同时过期
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductMapper productMapper;

    @Resource
    private SkuMapper skuMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private Cache<Long, Product> productCache;          // L1: 商品详情本地缓存

    @Resource
    private Cache<String, List<Category>> categoryCache; // L1: 分类列表本地缓存

    /** 用于将 Redis 反序列化的 Map 转回实体对象 */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ==================== Redis Key 常量 ====================

    private static final String PRODUCT_KEY = "cache:product:";    // 商品详情
    private static final String SKU_LIST_KEY = "cache:sku:list:";  // SKU列表
    private static final String CATEGORY_KEY = "cache:category:all"; // 全部分类
    private static final String PRODUCT_LIST_KEY = "cache:product:list:"; // 商品列表

    private static final long PRODUCT_TTL_MINUTES = 30;
    private static final long NULL_TTL_MINUTES = 3;   // 空值短缓存防穿透

    /**
     * 过期时间加随机偏移 — 防止缓存雪崩
     */
    private Duration randomTtl(long baseMinutes) {
        int offset = ThreadLocalRandom.current().nextInt(5); // 0~4分钟随机
        return Duration.ofMinutes(baseMinutes + offset);
    }

    // ==================== 商品详情（两级缓存，Cache-Aside模式） ====================

    @Override
    public Product getById(Long id) {
        if (id == null) return null;

        // ① L1: 查本地缓存（纳秒级，不走网络）
        Product cached = productCache.getIfPresent(id);
        if (cached != null) {
            return isNullObject(cached) ? null : cached;
        }

        // ② L2: 查 Redis（毫秒级，一次网络往返）
        String key = PRODUCT_KEY + id;
        Object obj = redisTemplate.opsForValue().get(key);
        Product fromRedis = toProduct(obj);
        if (fromRedis != null) {
            productCache.put(id, fromRedis);
            return isNullObject(fromRedis) ? null : fromRedis;
        }

        // ③ Redis也没有 → 加分布式锁重建缓存（防缓存击穿）
        return rebuildProductCache(id, key);
    }

    /**
     * 安全转换 Redis 缓存对象 → Product
     * 无类型信息时反序列化得到 LinkedHashMap，需要手动转
     */
    @SuppressWarnings("unchecked")
    private Product toProduct(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Product p) return p;
        if (obj instanceof Map map) {
            try {
                return MAPPER.convertValue(map, Product.class);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 互斥锁重建缓存 — 防止热点key过期瞬间大量请求打到DB
     *
     * 关键: 只让一个线程去查DB重建，其他线程等或直接返回
     */
    private Product rebuildProductCache(Long id, String key) {
        RLock lock = redissonClient.getLock("lock:cache:product:" + id);
        try {
            // 尝试加锁，最多等1秒，锁10秒自动释放
            if (lock.tryLock(1, 10, TimeUnit.SECONDS)) {
                // 双重检查 — 可能别的线程已经重建好了
                Object obj = redisTemplate.opsForValue().get(key);
                Product existing = toProduct(obj);
                if (existing != null) {
                    productCache.put(id, existing);
                    return isNullObject(existing) ? null : existing;
                }

                // 查数据库
                Product dbProduct = productMapper.selectById(id);

                if (dbProduct != null) {
                    // 写入 Redis + 本地缓存
                    redisTemplate.opsForValue().set(key, dbProduct, randomTtl(PRODUCT_TTL_MINUTES));
                    productCache.put(id, dbProduct);
                    return dbProduct;
                } else {
                    // 空对象也缓存（短过期）→ 防缓存穿透
                    Product nullMarker = new Product();
                    nullMarker.setId(-1L); // 标记为空对象
                    redisTemplate.opsForValue().set(key, nullMarker, Duration.ofMinutes(NULL_TTL_MINUTES));
                    return null;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        // 没抢到锁 → 直接查一次DB兜底
        return productMapper.selectById(id);
    }

    // ==================== 商品列表（Redis缓存 + DB兜底） ====================

    @Override
    public List<Product> list(Long categoryId, String keyword) {
        // 生成缓存key
        String cacheKey;
        if (keyword != null && !keyword.isEmpty()) {
            cacheKey = PRODUCT_LIST_KEY + "search:" + keyword;
        } else if (categoryId != null && categoryId > 0) {
            cacheKey = PRODUCT_LIST_KEY + "category:" + categoryId;
        } else {
            cacheKey = PRODUCT_LIST_KEY + "all";
        }

        // 查 Redis
        @SuppressWarnings("unchecked")
        List<Product> cached = (List<Product>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) return cached;

        // 查数据库
        List<Product> list;
        if (keyword != null && !keyword.isEmpty()) {
            list = productMapper.search(keyword);
        } else if (categoryId != null && categoryId > 0) {
            list = productMapper.selectByCategoryId(categoryId);
        } else {
            list = productMapper.selectListOnShelf();
        }

        // 写入 Redis（5分钟过期，列表数据变化快，TTL短一些）
        if (list != null && !list.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, list, Duration.ofMinutes(5));
        }
        return list;
    }

    // ==================== SKU列表 ====================
    // SKU和库存关联紧密，过期时间设短（防库存变化后读到旧数据）

    @Override
    public List<Sku> getSkuList(Long productId) {
        String key = SKU_LIST_KEY + productId;
        @SuppressWarnings("unchecked")
        List<Sku> cached = (List<Sku>) redisTemplate.opsForValue().get(key);
        if (cached != null) return cached;

        List<Sku> list = skuMapper.selectByProductId(productId);
        if (list != null && !list.isEmpty()) {
            redisTemplate.opsForValue().set(key, list, Duration.ofMinutes(10));
        }
        return list;
    }

    // ==================== 分类列表（两级缓存，几乎不变） ====================

    @Override
    public List<Category> getAllCategories() {
        // L1: 本地缓存
        List<Category> cached = categoryCache.getIfPresent(CATEGORY_KEY);
        if (cached != null) return cached;

        // L2: Redis
        @SuppressWarnings("unchecked")
        List<Category> redisList = (List<Category>) redisTemplate.opsForValue().get(CATEGORY_KEY);
        if (redisList != null) {
            categoryCache.put(CATEGORY_KEY, redisList);
            return redisList;
        }

        // DB
        List<Category> list = categoryMapper.selectList(null);
        if (list != null && !list.isEmpty()) {
            categoryCache.put(CATEGORY_KEY, list);
            redisTemplate.opsForValue().set(CATEGORY_KEY, list, Duration.ofHours(1)); // 分类1小时不过期
        }
        return list;
    }

    // ==================== 缓存失效方法 ====================
    // 数据变更时调用，删除Redis缓存，本地缓存靠TTL自动过期

    /** 商品变更后，删除相关缓存 */
    public void evictProductCache(Long productId) {
        redisTemplate.delete(PRODUCT_KEY + productId);
        redisTemplate.delete(SKU_LIST_KEY + productId);
        redisTemplate.delete(PRODUCT_LIST_KEY + "all");
        // 分类列表缓存不用删（商品变更不影响分类结构）
    }

    /** 分类变更后，删除分类缓存 */
    public void evictCategoryCache() {
        redisTemplate.delete(CATEGORY_KEY);
    }

    // ==================== 工具方法 ====================

    private boolean isNullObject(Product p) {
        return p.getId() != null && p.getId() == -1L;
    }
}
