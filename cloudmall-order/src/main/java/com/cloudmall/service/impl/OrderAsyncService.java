package com.cloudmall.service.impl;

import com.cloudmall.entity.Order;
import com.cloudmall.entity.Sku;
import com.cloudmall.mapper.SkuMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 订单异步服务 — 下单后的非关键路径操作
 *
 * 为什么拆出来？
 * Spring @Async 基于AOP代理，同一个类里调用不走代理 → @Async不生效
 * 独立一个Bean，让Spring代理拦截 → @Async 才真正异步执行
 */
@Service
public class OrderAsyncService {

    @Resource
    private SkuMapper skuMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 异步失效商品缓存 — 不阻塞用户拿订单结果
     */
    @Async("orderExecutor")
    public void invalidateProductCaches(List<Map<String, Object>> cartItems) {
        Set<Long> productIds = cartItems.stream()
                .map(item -> {
                    Long skuId = Long.valueOf(item.get("skuId").toString());
                    Sku sku = skuMapper.selectById(skuId);
                    return sku != null ? sku.getProductId() : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (Long productId : productIds) {
            redisTemplate.delete("cache:product:" + productId);
            redisTemplate.delete("cache:sku_list:" + productId);
            redisTemplate.delete("cache:product:all");
        }
    }

    /**
     * 异步发送下单通知 — 短信/推送等
     */
    @Async("orderExecutor")
    public void sendOrderNotification(Order order) {
        // TODO: 接入真实通知服务
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
