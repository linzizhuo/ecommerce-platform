package com.cloudmall.service.impl;

import com.cloudmall.common.exception.BusinessException;
import com.cloudmall.entity.*;
import com.cloudmall.mapper.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 秒杀服务 — Redis缓存场次 + 互斥锁防击穿 + Lua原子扣库存 + 批量异步下单
 *
 * 核心流程:
 * 1. 从Redis读取场次缓存（miss时互斥锁查DB回种，其余线程随机退避重试）
 * 2. 校验时间
 * 3. Lua脚本原子操作：检查参与 + 初始化库存 + 扣库存 + 标记已抢（一个往返，全成功或全失败）
 * 4. 扔进Redis队列，立即返回
 * 5. 后台批量消费队列（每次最多50条，500ms间隔）
 */
@Service
public class SeckillService {

    private static final Logger log = LoggerFactory.getLogger(SeckillService.class);

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
    @Lazy
    @Resource
    private SeckillService self;  // 自注入，使 @Transactional 在 @Scheduled 内部调用生效

    private static final String STOCK_KEY = "seckill:stock:";
    private static final String RECORD_KEY = "seckill:record:";
    private static final String SESSION_KEY = "seckill:session:";
    private static final String LOCK_KEY = "seckill:lock:session:";
    private static final String QUEUE_KEY = "seckill:queue";
    private static final String QUEUE_BACKUP_KEY = "seckill:queue:backup";
    private static final String RESULT_KEY = "seckill:result:";
    private static final String SESSIONS_CACHE_KEY = "seckill:sessions:active";

    /** Lua脚本：原子检查参与 + 初始化库存 + 扣库存 + 标记已抢，一个往返全部搞定 */
    private DefaultRedisScript<Long> seckillScript;

    @PostConstruct
    public void init() {
        seckillScript = new DefaultRedisScript<>();
        seckillScript.setResultType(Long.class);
        seckillScript.setScriptText(
            "-- KEYS[1]=recordKey  KEYS[2]=stockKey  ARGV[1]=initialStock\n" +
            "-- 返回: -1=已参与  -2=已售罄  >=0=成功(剩余库存)\n" +
            "if redis.call('EXISTS', KEYS[1]) == 1 then\n" +
            "  return -1\n" +
            "end\n" +
            "redis.call('SETNX', KEYS[2], ARGV[1])\n" +
            "local stock = redis.call('DECR', KEYS[2])\n" +
            "if stock < 0 then\n" +
            "  redis.call('INCR', KEYS[2])\n" +
            "  return -2\n" +
            "end\n" +
            "redis.call('SETEX', KEYS[1], 86400, '1')\n" +
            "return stock");

        // 恢复上次未处理完的孤儿消息
        recoverOrphanMessages();
    }

    /** 启动时把备份队列的孤儿消息搬回主队列 */
    private void recoverOrphanMessages() {
        try {
            Long size = redisTemplate.opsForList().size(QUEUE_BACKUP_KEY);
            if (size != null && size > 0) {
                List<Object> orphans = redisTemplate.opsForList()
                        .range(QUEUE_BACKUP_KEY, 0, -1);
                redisTemplate.delete(QUEUE_BACKUP_KEY);
                if (orphans != null && !orphans.isEmpty()) {
                    redisTemplate.opsForList().leftPushAll(QUEUE_KEY, orphans);
                    log.warn("恢复孤儿秒杀消息: {} 条，已重新入队", orphans.size());
                }
            }
        } catch (Exception e) {
            log.error("恢复孤儿消息失败: {}", e.getMessage());
        }
    }

    /**
     * 活动开始前预热秒杀场次到Redis
     * 缓存整个场次元数据 + 初始化库存计数器
     * TTL = 活动结束时间 - 当前时间 + 10分钟缓冲
     */
    public void warmUp(Long sessionId) {
        SeckillSession session = sessionMapper.selectById(sessionId);
        if (session == null) return;

        Duration ttl = Duration.between(LocalDateTime.now(), session.getEndTime()).plusMinutes(10);
        if (ttl.isNegative() || ttl.isZero()) {
            ttl = Duration.ofMinutes(5);
        }

        String sessionKey = SESSION_KEY + sessionId;
        redisTemplate.opsForValue().set(sessionKey, session, ttl);

        String stockKey = STOCK_KEY + sessionId;
        redisTemplate.opsForValue().set(stockKey, session.getStock(), ttl);

        log.info("秒杀预热完成: sessionId={}, stock={}, ttl={}s", sessionId, session.getStock(), ttl.getSeconds());
    }

    /**
     * 从Redis读取场次缓存 — 互斥锁 + 随机退避重试
     *
     * 缓存miss时：抢互斥锁，只让一个线程查DB回种。
     * 没抢到锁的线程：先自旋等缓存 → 等不到就随机退避后重新抢锁（最多3轮）。
     */
    private SeckillSession getSessionFromCache(Long sessionId) {
        String sessionKey = SESSION_KEY + sessionId;
        String lockKey = LOCK_KEY + sessionId;

        for (int retry = 0; retry < 3; retry++) {
            Object cached = redisTemplate.opsForValue().get(sessionKey);
            if (cached instanceof SeckillSession) {
                return (SeckillSession) cached;
            }

            Boolean gotLock = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "1", Duration.ofSeconds(3));

            if (Boolean.TRUE.equals(gotLock)) {
                try {
                    cached = redisTemplate.opsForValue().get(sessionKey);
                    if (cached instanceof SeckillSession) {
                        return (SeckillSession) cached;
                    }
                    SeckillSession session = sessionMapper.selectById(sessionId);
                    if (session != null) {
                        Duration ttl = Duration.between(LocalDateTime.now(), session.getEndTime()).plusMinutes(10);
                        if (ttl.isNegative() || ttl.isZero()) ttl = Duration.ofMinutes(5);
                        redisTemplate.opsForValue().set(sessionKey, session, ttl);
                        log.info("秒杀缓存回种: sessionId={}, ttl={}s", sessionId, ttl.getSeconds());
                    }
                    return session;
                } finally {
                    redisTemplate.delete(lockKey);
                }
            }

            // 没抢到锁 — 自旋等缓存
            for (int i = 0; i < 10; i++) {
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                cached = redisTemplate.opsForValue().get(sessionKey);
                if (cached instanceof SeckillSession) {
                    return (SeckillSession) cached;
                }
            }

            // 随机退避，下一轮重新抢锁
            if (retry < 2) {
                int backoff = 100 + ThreadLocalRandom.current().nextInt(400);
                try { Thread.sleep(backoff); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
        }

        log.error("秒杀缓存获取失败，拒绝请求: sessionId={}", sessionId);
        throw new BusinessException("系统繁忙，请稍后再试");
    }

    /** 秒杀下单 */
    public Map<String, Object> seckill(Long userId, Long sessionId) {
        // 1. 从Redis读场次
        SeckillSession session = getSessionFromCache(sessionId);
        if (session == null) throw new BusinessException("秒杀活动不存在");

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(session.getStartTime())) throw new BusinessException("活动未开始");
        if (now.isAfter(session.getEndTime())) throw new BusinessException("活动已结束");

        // 2. 空库存快速拦截：售罄后直接用GET判断，省去Lua脚本的Redis往返
        String stockKey = STOCK_KEY + session.getId();
        Object stockObj = redisTemplate.opsForValue().get(stockKey);
        if (stockObj instanceof Number && ((Number) stockObj).intValue() <= 0)
            throw new BusinessException("已售罄");

        // 3. Lua原子操作：检查参与 + 初始化库存 + 扣库存 + 标记已抢（一个Redis往返）
        String recordKey = RECORD_KEY + sessionId + ":" + userId;
        Long result = redisTemplate.execute(
                seckillScript,
                Arrays.asList(recordKey, stockKey),
                session.getStock());

        if (result == null) throw new BusinessException("系统繁忙");
        if (result == -1) throw new BusinessException("您已参与本次秒杀");
        if (result == -2) throw new BusinessException("已售罄");

        // 4. 扔进异步队列 + 标记结果pending
        Map<String, Object> msg = new HashMap<>();
        msg.put("userId", userId);
        msg.put("sessionId", sessionId);
        msg.put("skuId", session.getSkuId());
        msg.put("seckillPrice", session.getSeckillPrice());
        msg.put("timestamp", System.currentTimeMillis());
        redisTemplate.opsForList().leftPush(QUEUE_KEY, msg);
        redisTemplate.opsForValue().set(RESULT_KEY + sessionId + ":" + userId, "PENDING", Duration.ofMinutes(5));

        // 5. 立即返回
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("message", "抢购成功，订单生成中...");
        resultMap.put("sessionId", sessionId);
        return resultMap;
    }

    /**
     * 后台消费队列 — 原子搬到备份队列 → 批量创建订单 → 成功后删除备份
     * 消息不丢：处理中崩溃 → 下次启动 recoverOrphanMessages() 搬回主队列
     */
    @Scheduled(fixedDelay = 500)
    public void processQueue() {
        List<Map<String, Object>> batch = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Object msg = redisTemplate.opsForList()
                    .rightPopAndLeftPush(QUEUE_KEY, QUEUE_BACKUP_KEY, Duration.ofMillis(100));
            if (msg == null) break;
            if (msg instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) msg;
                batch.add(data);
            }
        }
        if (!batch.isEmpty()) {
            try {
                self.batchCreateOrders(batch);  // 通过代理走 @Transactional
                // 全部成功 → 从备份队列清除
                for (Map<String, Object> data : batch) {
                    redisTemplate.opsForList().remove(QUEUE_BACKUP_KEY, 1, data);
                }
            } catch (Exception e) {
                log.error("批量创建秒杀订单失败: {}", e.getMessage());
            }
        }
    }

    /** 批量创建秒杀订单 — saveBatch 替代逐条 insert */
    @Transactional
    public void batchCreateOrders(List<Map<String, Object>> batch) {
        List<Order> orders = new ArrayList<>();
        List<OrderItem> items = new ArrayList<>();
        List<Map<String, Object>> successList = new ArrayList<>();

        for (Map<String, Object> data : batch) {
            try {
                Long userId = Long.valueOf(data.get("userId").toString());
                Long skuId = Long.valueOf(data.get("skuId").toString());
                Long sessionId = Long.valueOf(data.get("sessionId").toString());
                Integer price = (Integer) data.get("seckillPrice");

                Sku sku = skuMapper.selectById(skuId);
                if (sku == null) {
                    redisTemplate.opsForValue().set(RESULT_KEY + sessionId + ":" + userId,
                            "FAILED", Duration.ofMinutes(5));
                    continue;
                }

                int rows = skuMapper.deductStock(skuId, 1);
                if (rows == 0) {
                    redisTemplate.opsForValue().set(RESULT_KEY + sessionId + ":" + userId,
                            "FAILED", Duration.ofMinutes(5));
                    continue;
                }

                Order order = new Order();
                order.setOrderNo("SK" + System.currentTimeMillis() + "_" + orders.size());
                order.setUserId(userId);
                order.setTotalAmount(price);
                order.setPayAmount(price);
                order.setStatus(0);
                order.setCreateTime(LocalDateTime.now());
                orders.add(order);
                successList.add(data);
            } catch (Exception e) {
                log.error("准备秒杀订单失败: {}", e.getMessage());
            }
        }

        if (!orders.isEmpty()) {
            // 逐条插入订单（MyBatis-Plus BaseMapper.insert 自动回填自增 ID）
            for (Order order : orders) {
                orderMapper.insert(order);
            }
            for (int i = 0; i < orders.size(); i++) {
                Order order = orders.get(i);
                Map<String, Object> data = successList.get(i);
                OrderItem item = new OrderItem();
                item.setOrderId(order.getId());
                item.setSkuId(Long.valueOf(data.get("skuId").toString()));
                item.setProductName("秒杀商品");
                item.setPrice((Integer) data.get("seckillPrice"));
                item.setQuantity(1);
                items.add(item);
            }
            for (OrderItem item : items) {
                orderItemMapper.insert(item);
            }

            // 写入订单结果到 Redis
            for (int i = 0; i < orders.size(); i++) {
                Map<String, Object> data = successList.get(i);
                String resultKey = RESULT_KEY + data.get("sessionId") + ":" + data.get("userId");
                redisTemplate.opsForValue().set(resultKey, orders.get(i).getId(), Duration.ofMinutes(5));
            }

            log.info("批量创建秒杀订单: {} 条", orders.size());
        }
    }

    /** 查询秒杀场次（Redis缓存 + 互斥锁防击穿，10秒刷新，库存实时从Redis读） */
    @SuppressWarnings("unchecked")
    public List<SeckillSession> activeSessions() {
        List<SeckillSession> sessions;

        Object cached = redisTemplate.opsForValue().get(SESSIONS_CACHE_KEY);
        if (cached instanceof List) {
            sessions = (List<SeckillSession>) cached;
        } else {
            String lockKey = "seckill:lock:sessions";
            Boolean gotLock = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "1", Duration.ofSeconds(2));

            if (Boolean.TRUE.equals(gotLock)) {
                try {
                    cached = redisTemplate.opsForValue().get(SESSIONS_CACHE_KEY);
                    if (cached instanceof List) {
                        sessions = (List<SeckillSession>) cached;
                    } else {
                        sessions = sessionMapper.selectList(
                                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SeckillSession>()
                                        .gt(SeckillSession::getEndTime, LocalDateTime.now())
                                        .orderByAsc(SeckillSession::getStartTime));
                        redisTemplate.opsForValue().set(SESSIONS_CACHE_KEY, sessions, Duration.ofSeconds(10));
                    }
                } finally {
                    redisTemplate.delete(lockKey);
                }
            } else {
                for (int i = 0; i < 5; i++) {
                    try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                    cached = redisTemplate.opsForValue().get(SESSIONS_CACHE_KEY);
                    if (cached instanceof List) {
                        sessions = (List<SeckillSession>) cached;
                        fillRealtimeStock(sessions);
                        return sessions;
                    }
                }
                sessions = sessionMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SeckillSession>()
                                .gt(SeckillSession::getEndTime, LocalDateTime.now())
                                .orderByAsc(SeckillSession::getStartTime));
            }
        }

        // 每次请求都从Redis取实时库存，覆盖缓存的DB初始值
        fillRealtimeStock(sessions);
        return sessions;
    }

    /** 查询秒杀订单结果（供前端轮询） */
    public Map<String, Object> getResult(Long sessionId, Long userId) {
        Object val = redisTemplate.opsForValue().get(RESULT_KEY + sessionId + ":" + userId);
        Map<String, Object> result = new HashMap<>();
        if (val == null) {
            result.put("status", "NOT_FOUND");
        } else if ("PENDING".equals(val)) {
            result.put("status", "PENDING");
        } else if ("FAILED".equals(val)) {
            result.put("status", "FAILED");
        } else if (val instanceof Number) {
            result.put("status", "SUCCESS");
            result.put("orderId", ((Number) val).longValue());
        }
        return result;
    }

    /** 从Redis读取每个场次的实时剩余库存（Pipeline合并为一个往返） */
    private void fillRealtimeStock(List<SeckillSession> sessions) {
        if (sessions.isEmpty()) return;
        List<Object> stocks = redisTemplate.executePipelined(
                new SessionCallback<Object>() {
                    @Override
                    public Object execute(org.springframework.data.redis.core.RedisOperations operations) {
                        for (SeckillSession s : sessions) {
                            operations.opsForValue().get(STOCK_KEY + s.getId());
                        }
                        return null;
                    }
                });
        for (int i = 0; i < sessions.size() && i < stocks.size(); i++) {
            Object stockObj = stocks.get(i);
            if (stockObj instanceof Number) {
                sessions.get(i).setStock(((Number) stockObj).intValue());
            }
        }
    }
}
