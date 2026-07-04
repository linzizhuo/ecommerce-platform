package com.cloudmall.service.impl;

import com.cloudmall.entity.Message;
import com.cloudmall.mapper.MessageMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 消息推送服务 — 站内信 + Redis发布订阅 + WebSocket实时推送
 */
@Service
public class MessageService {

    @Resource private MessageMapper messageMapper;
    @Resource private RedisTemplate<String, Object> redisTemplate;

    /** 异步发送消息 */
    @Async("orderExecutor")
    public void send(Long userId, String title, String content, Integer type, Long orderId) {
        Message msg = new Message();
        msg.setUserId(userId);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setType(type != null ? type : 1);
        msg.setOrderId(orderId);
        msg.setIsRead(0);
        messageMapper.insert(msg);
        // Redis发布：WebSocket订阅者收到后推送给浏览器
        redisTemplate.convertAndSend("message:" + userId, title);
    }

    /** 用户消息列表 */
    public List<Message> userMessages(Long userId, Integer isRead) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Message> qw =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Message>()
                        .eq(Message::getUserId, userId)
                        .orderByDesc(Message::getCreateTime);
        if (isRead != null) qw.eq(Message::getIsRead, isRead);
        return messageMapper.selectList(qw);
    }

    /** 标记已读 */
    public void markRead(Long messageId, Long userId) {
        Message msg = messageMapper.selectById(messageId);
        if (msg != null && msg.getUserId().equals(userId)) {
            msg.setIsRead(1);
            messageMapper.updateById(msg);
        }
    }

    /** 未读消息数 */
    public int unreadCount(Long userId) {
        Long count = messageMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Message>()
                        .eq(Message::getUserId, userId)
                        .eq(Message::getIsRead, 0));
        return count.intValue();
    }

    /** 全部已读 */
    public void markAllRead(Long userId) {
        List<Message> unread = userMessages(userId, 0);
        for (Message msg : unread) {
            msg.setIsRead(1);
            messageMapper.updateById(msg);
        }
    }
}
