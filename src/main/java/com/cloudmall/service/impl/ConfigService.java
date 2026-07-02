package com.cloudmall.service.impl;

import com.cloudmall.entity.*;
import com.cloudmall.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

/**
 * 系统配置/公告/弹窗服务 — 管理后台可控的全局参数
 */
@Service
public class ConfigService {

    @Resource private SystemConfigMapper configMapper;
    @Resource private NoticeMapper noticeMapper;
    @Resource private RedisTemplate<String, Object> redisTemplate;

    // ==================== 系统配置 ====================
    public String getValue(String key) {
        String cacheKey = "config:" + key;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) return cached.toString();

        SystemConfig config = configMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getConfigKey, key));
        String value = config != null ? config.getConfigValue() : null;
        if (value != null) redisTemplate.opsForValue().set(cacheKey, value, Duration.ofHours(1));
        return value;
    }

    public List<SystemConfig> allConfigs() { return configMapper.selectList(null); }

    public void saveConfig(SystemConfig config) {
        if (config.getId() == null) configMapper.insert(config);
        else configMapper.updateById(config);
        redisTemplate.delete("config:" + config.getConfigKey()); // 失效缓存
    }

    // ==================== 公告管理 ====================
    public List<Notice> activeNotices() {
        String key = "notice:active";
        @SuppressWarnings("unchecked")
        List<Notice> cached = (List<Notice>) redisTemplate.opsForValue().get(key);
        if (cached != null) return cached;

        List<Notice> list = noticeMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Notice>()
                        .eq(Notice::getStatus, 1)
                        .orderByDesc(Notice::getIsTop)
                        .orderByDesc(Notice::getCreateTime));
        redisTemplate.opsForValue().set(key, (Object) list, Duration.ofMinutes(10));
        return list;
    }

    public List<Notice> listNotices() { return noticeMapper.selectList(null); }

    public void saveNotice(Notice notice) {
        if (notice.getId() == null) noticeMapper.insert(notice);
        else noticeMapper.updateById(notice);
        redisTemplate.delete("notice:active");
    }

    public void deleteNotice(Long id) {
        noticeMapper.deleteById(id);
        redisTemplate.delete("notice:active");
    }
}
