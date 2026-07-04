package com.cloudmall.service.impl;

import com.cloudmall.common.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 购物车服务 — Redis Hash实现
 * Key: cart:{userId}
 * Field: skuId
 * Value: JSON {skuId, productName, specInfo, price, quantity, image, checked}
 */
@Service
public class CartServiceImpl {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private String key(Long userId) { return "cart:" + userId; }

    /** 加入购物车 */
    public void addItem(Long userId, Long skuId, String productName,
                        String specInfo, Integer price, Integer quantity, String image) {
        String k = key(userId);
        Object existing = redisTemplate.opsForHash().get(k, skuId.toString());
        if (existing != null) {
            // 已存在，数量累加
            Map<String, Object> item = (Map<String, Object>) existing;
            item.put("quantity", (Integer) item.get("quantity") + quantity);
            redisTemplate.opsForHash().put(k, skuId.toString(), item);
        } else {
            Map<String, Object> item = new HashMap<>();
            item.put("skuId", skuId);
            item.put("productName", productName);
            item.put("specInfo", specInfo);
            item.put("price", price);
            item.put("quantity", quantity);
            item.put("image", image);
            item.put("checked", true);
            redisTemplate.opsForHash().put(k, skuId.toString(), item);
        }
    }

    /** 获取购物车 */
    public List<Map<String, Object>> list(Long userId) {
        return redisTemplate.opsForHash().values(key(userId)).stream()
                .map(v -> (Map<String, Object>) v)
                .collect(Collectors.toList());
    }

    /** 修改数量 */
    public void updateQuantity(Long userId, Long skuId, Integer quantity) {
        String k = key(userId);
        Map<String, Object> item = (Map<String, Object>) redisTemplate.opsForHash().get(k, skuId.toString());
        if (item == null) throw new BusinessException("购物车无此商品");
        if (quantity <= 0) {
            redisTemplate.opsForHash().delete(k, skuId.toString());
        } else {
            item.put("quantity", quantity);
            redisTemplate.opsForHash().put(k, skuId.toString(), item);
        }
    }

    /** 删除 */
    public void removeItem(Long userId, Long skuId) {
        redisTemplate.opsForHash().delete(key(userId), skuId.toString());
    }

    /** 勾选 */
    public void checkItem(Long userId, Long skuId, Boolean checked) {
        String k = key(userId);
        Map<String, Object> item = (Map<String, Object>) redisTemplate.opsForHash().get(k, skuId.toString());
        if (item != null) {
            item.put("checked", checked);
            redisTemplate.opsForHash().put(k, skuId.toString(), item);
        }
    }

    /** 获取选中商品 */
    public List<Map<String, Object>> getCheckedItems(Long userId) {
        return list(userId).stream()
                .filter(item -> (Boolean) item.getOrDefault("checked", true))
                .collect(Collectors.toList());
    }

    /** 清空已选 */
    public void removeChecked(Long userId) {
        String k = key(userId);
        List<Map<String, Object>> items = list(userId);
        for (Map<String, Object> item : items) {
            if ((Boolean) item.getOrDefault("checked", true)) {
                redisTemplate.opsForHash().delete(k, item.get("skuId").toString());
            }
        }
    }

    /** 清空购物车 */
    public void clear(Long userId) {
        redisTemplate.delete(key(userId));
    }
}
