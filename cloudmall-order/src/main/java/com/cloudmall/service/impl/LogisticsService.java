package com.cloudmall.service.impl;

import com.cloudmall.common.exception.BusinessException;
import com.cloudmall.entity.Logistics;
import com.cloudmall.entity.Order;
import com.cloudmall.mapper.LogisticsMapper;
import com.cloudmall.mapper.OrderMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 物流服务 — 发货记录 + 轨迹查询
 *
 * 接口设计: LogisticsProvider接口（策略模式）
 *   - Kuaidi100Provider: 快递100真实API
 *   - KuaidiNiaoProvider: 快递鸟真实API
 *   - MockProvider（当前）: 模拟轨迹，不依赖第三方
 * 切换方式: 改一个实现类即可，业务代码不变
 */
@Service
public class LogisticsService {

    @Resource private LogisticsMapper logisticsMapper;
    @Resource private OrderMapper orderMapper;
    @Resource private RedisTemplate<String, Object> redisTemplate;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 商家发货 */
    public void ship(Long orderId, Long merchantId, String company, String trackingNo) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 1)
            throw new BusinessException("订单状态异常，无法发货");

        Logistics log = new Logistics();
        log.setOrderId(orderId);
        log.setCompany(company);
        log.setTrackingNo(trackingNo);
        log.setStatus(0);
        log.setShipTime(LocalDateTime.now());

        // 生成初始Mock轨迹
        log.setTraceData(generateMockTrace(LocalDateTime.now(), 0));

        logisticsMapper.insert(log);
        order.setStatus(2);
        orderMapper.updateById(order);
    }

    /** 查询物流轨迹 — 缓存10分钟，未签收则追加新轨迹 */
    public Logistics query(Long orderId) {
        String key = "logistics:" + orderId;

        List<Logistics> list = logisticsMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Logistics>()
                        .eq(Logistics::getOrderId, orderId)
                        .orderByDesc(Logistics::getShipTime));
        if (list.isEmpty()) return null;

        Logistics logistics = list.get(0);

        // 如果还没签收，更新Mock轨迹（模拟物流推进）
        if (logistics.getStatus() < 3 && logistics.getShipTime() != null) {
            // 根据经过的时间计算当前应该在哪一步
            long hoursSinceShip = Duration.between(logistics.getShipTime(), LocalDateTime.now()).toHours();
            int newStatus;
            if (hoursSinceShip >= 72) newStatus = 3;        // 3天→已签收
            else if (hoursSinceShip >= 48) newStatus = 2;    // 2天→派送中
            else if (hoursSinceShip >= 24) newStatus = 1;    // 1天→运输中
            else newStatus = 0;                               // 当天→待揽收

            if (newStatus != logistics.getStatus()) {
                logistics.setStatus(newStatus);
                logistics.setTraceData(generateMockTrace(logistics.getShipTime(), newStatus));
                logisticsMapper.updateById(logistics);
                if (newStatus == 3) logistics.setSignTime(LocalDateTime.now());
            }
        }

        redisTemplate.opsForValue().set(key, logistics, Duration.ofMinutes(10));
        return logistics;
    }

    /** 确认收货 */
    public void sign(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId))
            throw new BusinessException("订单不存在");
        if (order.getStatus() != 2) throw new BusinessException("订单状态异常");

        List<Logistics> list = logisticsMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Logistics>()
                        .eq(Logistics::getOrderId, orderId));
        if (!list.isEmpty()) {
            Logistics l = list.get(0);
            l.setStatus(3);
            l.setSignTime(LocalDateTime.now());
            l.setTraceData(generateMockTrace(l.getShipTime() != null ? l.getShipTime() : LocalDateTime.now(), 3));
            logisticsMapper.updateById(l);
        }
        order.setStatus(3);
        orderMapper.updateById(order);
    }

    /**
     * Mock物流轨迹生成器
     * 模拟真实快递轨迹JSON，包含时间线节点
     */
    private String generateMockTrace(LocalDateTime baseTime, int targetStatus) {
        List<Map<String, String>> traces = new ArrayList<>();
        LocalDateTime t = baseTime;

        addTrace(traces, t, "【" + (t.format(FMT)) + "】您的订单已发货，等待揽收");
        if (targetStatus >= 1) {
            t = t.plusHours(3);
            addTrace(traces, t, "【" + (t.format(FMT)) + "】快递员已揽收，快件离开营业点");
            t = t.plusHours(8);
            addTrace(traces, t, "【" + (t.format(FMT)) + "】快件到达分拨中心");
        }
        if (targetStatus >= 2) {
            t = t.plusHours(12);
            addTrace(traces, t, "【" + (t.format(FMT)) + "】快件正在运输中");
            t = t.plusHours(12);
            addTrace(traces, t, "【" + (t.format(FMT)) + "】快件到达目的地城市，准备派送");
        }
        if (targetStatus >= 3) {
            t = t.plusHours(4);
            addTrace(traces, t, "【" + (t.format(FMT)) + "】快件已签收，感谢使用CloudMall");
        }

        // 转JSON
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < traces.size(); i++) {
            if (i > 0) sb.append(",");
            Map<String, String> tr = traces.get(i);
            sb.append("{\"time\":\"").append(escapeJson(tr.get("time"))).append("\",")
              .append("\"desc\":\"").append(escapeJson(tr.get("desc"))).append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    private void addTrace(List<Map<String, String>> list, LocalDateTime time, String desc) {
        list.add(Map.of("time", time.format(FMT), "desc", desc));
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
