package com.cloudmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.entity.*;
import com.cloudmall.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * 数据统计服务 — 看板数据 / 预计算 / Redis计数器
 */
@Service
public class StatisticsService {

    @Resource private StatDailyMapper statDailyMapper;
    @Resource private OrderMapper orderMapper;
    @Resource private UserMapper userMapper;
    @Resource private RedisTemplate<String, Object> redisTemplate;

    /** 今日实时数据（Redis计数器） */
    public Map<String, Object> todaySnapshot() {
        String today = LocalDate.now().toString();
        Map<String, Object> data = new HashMap<>();
        data.put("orderCount", getRedisCounter("stat:" + today + ":orders"));
        data.put("payAmount", getRedisCounter("stat:" + today + ":amount"));
        data.put("newUsers", getRedisCounter("stat:" + today + ":users"));
        return data;
    }

    private long getRedisCounter(String key) {
        Object v = redisTemplate.opsForValue().get(key);
        return v != null ? Long.parseLong(v.toString()) : 0;
    }

    /** 下单时增加Redis计数器（异步调用） */
    public void incrementOrderStats() {
        String today = LocalDate.now().toString();
        redisTemplate.opsForValue().increment("stat:" + today + ":orders");
    }

    /** 新增用户时增加计数器 */
    public void incrementUserStats() {
        String today = LocalDate.now().toString();
        redisTemplate.opsForValue().increment("stat:" + today + ":users");
    }

    /** 每日统计数据 */
    public StatDaily getDailyStat(LocalDate date) {
        List<StatDaily> list = statDailyMapper.selectList(
                new LambdaQueryWrapper<StatDaily>().eq(StatDaily::getStatDate, date));
        return list.isEmpty() ? null : list.get(0);
    }

    /** 近7天统计 */
    public List<StatDaily> weekStats() {
        LocalDate today = LocalDate.now();
        return statDailyMapper.selectList(
                new LambdaQueryWrapper<StatDaily>()
                        .ge(StatDaily::getStatDate, today.minusDays(7))
                        .orderByAsc(StatDaily::getStatDate));
    }

    /** 近30天统计 */
    public List<StatDaily> monthStats() {
        LocalDate today = LocalDate.now();
        return statDailyMapper.selectList(
                new LambdaQueryWrapper<StatDaily>()
                        .ge(StatDaily::getStatDate, today.minusDays(30))
                        .orderByAsc(StatDaily::getStatDate));
    }

    /** 订单状态分布 */
    public Map<Integer, Long> orderStatusDistribution() {
        List<Order> orders = orderMapper.selectList(null);
        Map<Integer, Long> dist = new LinkedHashMap<>();
        for (Order o : orders) {
            dist.merge(o.getStatus(), 1L, Long::sum);
        }
        return dist;
    }
}
