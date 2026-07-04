package com.cloudmall.common.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.cloudmall.entity.Category;
import com.cloudmall.entity.Product;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 两级缓存配置:
 * L1 — Caffeine 本地缓存（JVM内存，纳秒级，容量小）
 * L2 — Redis 分布式缓存（网络IO，毫秒级，容量大）
 *
 * 缓存策略:
 * - 商品详情: 本地5分钟 + Redis 30分钟（加随机值防雪崩）
 * - 商品分类: 本地30分钟（几乎不变的数据，长时间缓存）
 * - 热点商品列表: 本地1分钟 + Redis 5分钟
 */
@Configuration
public class CacheConfig {

    /**
     * 商品详情本地缓存 — 热点数据，访问频率最高
     * 最多存1000条，5分钟过期
     */
    @Bean
    public Cache<Long, Product> productCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    /**
     * 分类列表本地缓存 — 几乎不变，长时间缓存
     */
    @Bean
    public Cache<String, List<Category>> categoryCache() {
        return Caffeine.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }
}
