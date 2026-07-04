package com.cloudmall.common.handler;

import com.cloudmall.common.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket消息推送处理器
 *
 * 流程:
 * 1. 用户打开App → WebSocket连接 → 携JWT token鉴权
 * 2. 鉴权通过 → 订阅Redis频道 message:{userId}
 * 3. MessageService发送消息 → redisTemplate.convertAndSend
 * 4. 消息到达 → WebSocket推送给浏览器
 */
@Component
public class MessageWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(MessageWebSocketHandler.class);
    // userId → WebSocketSession 映射
    private static final Map<Long, WebSocketSession> SESSION_MAP = new ConcurrentHashMap<>();

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisMessageListenerContainer listenerContainer;

    public MessageWebSocketHandler(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        // 创建Redis订阅容器
        this.listenerContainer = new RedisMessageListenerContainer();
        this.listenerContainer.setConnectionFactory(
                redisTemplate.getConnectionFactory());
        this.listenerContainer.afterPropertiesSet();
        this.listenerContainer.start();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 从URL提取token: ws://host/ws/message?token=xxx
        String query = session.getUri() != null ? session.getUri().getQuery() : "";
        String token = null;
        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("token=")) {
                    token = param.substring(6);
                    break;
                }
            }
        }

        if (token == null || !JwtUtil.validate(token)) {
            try { session.close(CloseStatus.POLICY_VIOLATION); } catch (IOException ignored) {}
            return;
        }

        Long userId = JwtUtil.getUserId(token);
        SESSION_MAP.put(userId, session);

        // 订阅该用户的Redis消息频道
        String channel = "message:" + userId;
        MessageListener listener = (Message message, byte[] pattern) -> {
            String msg = new String(message.getBody(), StandardCharsets.UTF_8);
            sendToUser(userId, msg);
        };
        listenerContainer.addMessageListener(listener, new ChannelTopic(channel));

        log.info("WebSocket connected: userId={}", userId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        SESSION_MAP.entrySet().removeIf(e -> e.getValue().equals(session));
    }

    /** 向指定用户推送消息 */
    public void sendToUser(Long userId, String message) {
        WebSocketSession session = SESSION_MAP.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("WebSocket send failed: userId={}", userId, e);
            }
        }
    }

    /** 获取在线用户数 */
    public int onlineCount() {
        return SESSION_MAP.size();
    }
}
