package com.cloudmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.entity.RedEnvelope;
import com.cloudmall.mapper.RedEnvelopeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RedEnvelopeService {

    @Resource
    private RedEnvelopeMapper redEnvelopeMapper;

    /** 发送红包 */
    @Transactional
    public RedEnvelope send(Long senderId, Integer amount, Integer type,
                             String message, Long orderId) {
        RedEnvelope re = new RedEnvelope();
        re.setSenderId(senderId);
        re.setAmount(amount);
        re.setType(type != null ? type : 1);
        re.setMessage(message);
        re.setOrderId(orderId);
        re.setStatus(0);
        re.setExpireTime(LocalDateTime.now().plusHours(24));
        re.setCreateTime(LocalDateTime.now());
        redEnvelopeMapper.insert(re);
        return re;
    }

    /** 领取红包 */
    @Transactional
    public RedEnvelope receive(Long redEnvelopeId, Long receiverId) {
        RedEnvelope re = redEnvelopeMapper.selectById(redEnvelopeId);
        if (re == null) throw new RuntimeException("红包不存在");
        if (re.getStatus() != 0) throw new RuntimeException("红包已被领取或已过期");
        if (re.getExpireTime() != null && re.getExpireTime().isBefore(LocalDateTime.now()))
            throw new RuntimeException("红包已过期");
        if (re.getSenderId().equals(receiverId)) throw new RuntimeException("不能领取自己的红包");

        re.setReceiverId(receiverId);
        re.setStatus(1);
        re.setReceiveTime(LocalDateTime.now());
        redEnvelopeMapper.updateById(re);
        return re;
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
