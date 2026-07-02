package com.cloudmall.service.impl;

import com.cloudmall.common.exception.BusinessException;
import com.cloudmall.entity.*;
import com.cloudmall.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 秒杀服务 — Redis预减库存 + 异步下单
 *
 * 核心流程:
 * 1. 前端防护（按钮置灰/验证码）
 * 2. Redis DECR预减库存
 * 3. 库存不足 → 直接返回"已售罄"
 * 4. 库存够 → 扔进Redis队列异步下单
 */
@Service
public class SeckillService {

    @Resource
    private SeckillSessionMapper sessionMapper;
    @Resource
    private SkuMapper skuMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderItemMapper orderItemMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String STOCK_KEY = "seckill:stock:";
    private static final String RECORD_KEY = "seckill:record:";

    /** 活动开始前预热秒杀库存到Redis */
    public void warmUp(Long sessionId) {
        SeckillSession session = sessionMapper.selectById(sessionId);
        if (session == null) return;
        String key = STOCK_KEY + session.getSkuId();
        redisTemplate.opsForValue().set(key, session.getStock(), Duration.ofHours(2));
    }

    /** 秒杀下单 */
    public Map<String, Object> seckill(Long userId, Long sessionId) {
        // 1. 校验秒杀场次
        SeckillSession session = sessionMapper.selectById(sessionId);
        if (session == null) throw new BusinessException("秒杀活动不存在");
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(session.getStartTime())) throw new BusinessException("活动未开始");
        if (now.isAfter(session.getEndTime())) throw new BusinessException("活动已结束");

        // 2. 防重复秒杀
        String recordKey = RECORD_KEY + sessionId + ":" + userId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(recordKey)))
            throw new BusinessException("您已参与本次秒杀");

        // 3. Redis预减库存（核心）
        String stockKey = STOCK_KEY + session.getSkuId();
        Long stock = redisTemplate.opsForValue().decrement(stockKey);
        if (stock == null || stock < 0) {
            // 恢复库存
            redisTemplate.opsForValue().increment(stockKey);
            throw new BusinessException("已售罄");
        }

        // 4. 标记已抢
        redisTemplate.opsForValue().set(recordKey, "1", Duration.ofHours(24));

        // 5. 异步下单（扔进队列）
        Map<String, Object> msg = new HashMap<>();
        msg.put("userId", userId);
        msg.put("sessionId", sessionId);
        msg.put("skuId", session.getSkuId());
        msg.put("seckillPrice", session.getSeckillPrice());
        msg.put("timestamp", System.currentTimeMillis());

        redisTemplate.opsForList().leftPush("seckill:queue", msg);

        // 6. 立即返回
        Map<String, Object> result = new HashMap<>();
        result.put("message", "抢购成功，订单生成中...");
        result.put("sessionId", sessionId);
        return result;
    }

    /** 后台消费队列 — 真正创建订单（定时任务或独立线程消费） */
    public void processQueue() {
        Object msg = redisTemplate.opsForList().rightPop("seckill:queue", Duration.ofSeconds(5));
        if (msg instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) msg;
            createSeckillOrder(data);
        }
    }

    private void createSeckillOrder(Map<String, Object> data) {
        Long userId = Long.valueOf(data.get("userId").toString());
        Long skuId = Long.valueOf(data.get("skuId").toString());
        Integer price = (Integer) data.get("seckillPrice");

        Sku sku = skuMapper.selectById(skuId);
        if (sku == null) return;

        // 真正扣DB库存
        int rows = skuMapper.deductStock(skuId, 1);
        if (rows == 0) return; // 库存不足

        // 生成订单
        Order order = new Order();
        order.setOrderNo("SK" + System.currentTimeMillis());
        order.setUserId(userId);
        order.setTotalAmount(price);
        order.setPayAmount(price);
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setSkuId(skuId);
        item.setProductName(sku.getProductId() != null ? "秒杀商品" : "商品");
        item.setPrice(price);
        item.setQuantity(1);
        orderItemMapper.insert(item);
    }

    /** 查询秒杀场次 */
    public List<SeckillSession> activeSessions() {
        return sessionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SeckillSession>()
                        .eq(SeckillSession::getStatus, 1)
                        .orderByAsc(SeckillSession::getStartTime));
    }
}
