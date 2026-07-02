package com.cloudmall.common.config;

import com.cloudmall.entity.Category;
import com.cloudmall.entity.Product;
import com.cloudmall.mapper.CategoryMapper;
import com.cloudmall.mapper.ProductMapper;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热器 — 应用启动时预加载热点数据到两级缓存
 *
 * 策略:
 * - 分类列表 → Caffeine本地缓存（几乎不变，长时间缓存）
 * - 热门商品(前50) → Redis缓存（首次查询即命中，避免冷启动）
 * - 预热失败不阻塞启动（catch异常，记录日志）
 *
 * 答辩关键: 没有预热，系统启动后前N个请求都是缓存miss→全部打DB→冷启动雪崩
 */
@Component
public class CacheWarmer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CacheWarmer.class);

    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private Cache<String, List<Category>> categoryCache;

    private static final String CATEGORY_KEY = "cache:category:all";
    private static final String PRODUCT_KEY = "cache:product:";

    @Override
    public void run(String... args) {
        log.info("========== 开始缓存预热 ==========");
        long start = System.currentTimeMillis();

        try {
            warmCategories();
            warmHotProducts();
        } catch (Exception e) {
            log.error("缓存预热失败，系统正常启动（降级走DB）: {}", e.getMessage());
        }

        long cost = System.currentTimeMillis() - start;
        log.info("========== 缓存预热完成，耗时 {} ms ==========", cost);
    }

    /** 预热分类列表 — L1本地 + L2 Redis */
    private void warmCategories() {
        List<Category> categories = categoryMapper.selectList(null);
        if (categories != null && !categories.isEmpty()) {
            categoryCache.put(CATEGORY_KEY, categories);
            redisTemplate.opsForValue().set(CATEGORY_KEY, categories, Duration.ofHours(1));
            log.info("预热分类: {} 条", categories.size());
        }
    }

    /** 预热热门商品(前50) — Redis缓存 */
    private void warmHotProducts() {
        List<Product> products = productMapper.selectListOnShelf();
        if (products == null || products.isEmpty()) return;

        int count = Math.min(products.size(), 50);
        for (int i = 0; i < count; i++) {
            Product p = products.get(i);
            // 随机TTL防雪崩（30~35分钟）
            int ttl = 30 + ThreadLocalRandom.current().nextInt(5);
            redisTemplate.opsForValue().set(PRODUCT_KEY + p.getId(), p, Duration.ofMinutes(ttl));
        }
        log.info("预热商品: {} 条 (前50)", count);
    }
}
