package com.cloudmall.common.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * 布隆过滤器配置 — 防缓存穿透的进阶方案
 *
 * 原理: 多个哈希函数映射到位数组
 * - 说"不存在" → 100%准确（一定不存在）
 * - 说"存在" → 可能误判（极小概率，可配置）
 *
 * 为什么比缓存空对象好？
 * - 10万个商品ID → 缓存空对象约需 50KB*10万≈5GB（每个空对象JSON序列化后很大）
 * - 布隆过滤器 → 10万个ID只需 ~1MB（位数组）
 * - 攻击者扫100万个不存在ID → 空对象方式全进Redis，布隆过滤器全在内存挡掉
 *
 * 预期误判率: 0.03（3%），即100个不存在ID中有3个会穿透到DB
 * 可接受 — 这3个走到DB后也会被缓存空对象兜底
 */
@Configuration
public class BloomFilterConfig {

    private static final Logger log = LoggerFactory.getLogger(BloomFilterConfig.class);

    @Resource
    private RedissonClient redissonClient;

    /** 布隆过滤器名称 */
    public static final String PRODUCT_BLOOM = "bloom:product:ids";

    /** 预计插入数量 */
    private static final long EXPECTED_INSERTIONS = 100_000L;
    /** 期望误判率 */
    private static final double FALSE_PROBABILITY = 0.03;

    @PostConstruct
    public void init() {
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(PRODUCT_BLOOM);
        // tryInit: 如果已经初始化过则跳过
        boolean initialized = bloomFilter.tryInit(EXPECTED_INSERTIONS, FALSE_PROBABILITY);
        if (initialized) {
            log.info("布隆过滤器初始化完成: expectedInsertions={}, falseProbability={}",
                    EXPECTED_INSERTIONS, FALSE_PROBABILITY);
            // TODO: 启动时从数据库加载所有商品ID→bloomFilter.add(productId)
            // 首次创建后一次性加载，后续启动tryInit返回false跳过
        }
    }

    /**
     * 对外暴露的检查方法
     * @return true=可能存在, false=一定不存在
     */
    public boolean mightContain(Long productId) {
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(PRODUCT_BLOOM);
        return bloomFilter.contains(productId);
    }

    /** 新增商品时添加到布隆过滤器 */
    public void add(Long productId) {
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(PRODUCT_BLOOM);
        bloomFilter.add(productId);
    }
}
