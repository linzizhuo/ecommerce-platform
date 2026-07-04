package com.cloudmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.common.exception.BusinessException;
import com.cloudmall.entity.Merchant;
import com.cloudmall.entity.Order;
import com.cloudmall.entity.RedEnvelope;
import com.cloudmall.mapper.MerchantMapper;
import com.cloudmall.mapper.OrderMapper;
import com.cloudmall.mapper.RedEnvelopeMapper;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedEnvelopeService {

    @Resource
    private RedEnvelopeMapper redEnvelopeMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private MerchantMapper merchantMapper;
    @Resource
    private RedissonClient redissonClient;

    /** 发送红包（商家给已完成订单的用户返利） */
    @Transactional
    public RedEnvelope send(Long senderId, Integer amount, Integer type,
                             String message, String orderNo) {
        // 1. 校验订单存在
        if (orderNo == null || orderNo.isBlank()) throw new RuntimeException("订单编号不能为空");
        Order order = orderMapper.selectOne(
            new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) throw new RuntimeException("订单不存在: " + orderNo);

        // 2. 校验订单状态（已付款/已发货/已完成才能发红包）
        if (order.getStatus() == null || order.getStatus() < 1 || order.getStatus() > 3) {
            throw new RuntimeException("订单状态不允许发送红包（需已付款/已发货/已完成）");
        }

        // 3. 校验发送者是商家
        Merchant merchant = merchantMapper.selectOne(
            new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, senderId));
        if (merchant == null) throw new RuntimeException("仅商家可发送红包");

        // 4. 订单有归属商家时，校验是否为该商家
        if (order.getMerchantId() != null && !order.getMerchantId().equals(merchant.getId())) {
            throw new RuntimeException("只能给自己的订单发送红包");
        }

        // 5. 红包金额不能超过订单实付金额
        if (amount > order.getPayAmount()) {
            throw new RuntimeException("红包金额不能超过订单实付金额 ¥" + order.getPayAmount() / 100.0);
        }

        RedEnvelope re = new RedEnvelope();
        re.setSenderId(senderId);
        re.setAmount(amount);
        re.setType(type != null ? type : 1);
        re.setMessage(message);
        re.setOrderId(order.getId());
        re.setReceiverId(order.getUserId());
        re.setStatus(0);
        re.setExpireTime(LocalDateTime.now().plusHours(24));
        re.setCreateTime(LocalDateTime.now());
        redEnvelopeMapper.insert(re);
        return re;
    }

    /** 领取红包（分布式锁防并发重复领取） */
    @Transactional
    public RedEnvelope receive(Long redEnvelopeId, Long receiverId) {
        RLock lock = redissonClient.getLock("redenvelope:receive:" + redEnvelopeId);
        try {
            if (!lock.tryLock(2, 5, TimeUnit.SECONDS))
                throw new BusinessException("红包领取繁忙，请稍后重试");

            RedEnvelope re = redEnvelopeMapper.selectById(redEnvelopeId);
            if (re == null) throw new RuntimeException("红包不存在");
            if (re.getStatus() != 0) throw new RuntimeException("红包已被领取或已过期");
            if (re.getExpireTime() != null && re.getExpireTime().isBefore(LocalDateTime.now()))
                throw new RuntimeException("红包已过期");

            re.setReceiverId(receiverId);
            re.setStatus(1);
            re.setReceiveTime(LocalDateTime.now());
            redEnvelopeMapper.updateById(re);
            return re;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统繁忙");
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    /** 我发送的红包 */
    public List<RedEnvelope> sentList(Long userId) {
        return redEnvelopeMapper.selectList(
            new LambdaQueryWrapper<RedEnvelope>()
                .eq(RedEnvelope::getSenderId, userId)
                .orderByDesc(RedEnvelope::getCreateTime));
    }

    /** 我收到的红包 */
    public List<RedEnvelope> receivedList(Long userId) {
        return redEnvelopeMapper.selectList(
            new LambdaQueryWrapper<RedEnvelope>()
                .eq(RedEnvelope::getReceiverId, userId)
                .orderByDesc(RedEnvelope::getReceiveTime));
    }
}
