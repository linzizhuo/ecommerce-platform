package com.cloudmall.service.impl;

import com.cloudmall.common.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 购物车服务 — Redis Hash实现
 * Key: cart:{userId}
 * Field: skuId
 * Value: JSON {skuId, productName, specInfo, price, quantity, image, checked}
 *
 * 使用 StringRedisTemplate + 手动JSON序列化，避免 GenericJackson2JsonRedisSerializer
 * 的 activateDefaultTyping 导致的 @class 类型元数据要求。
 * 这样新旧数据（带/不带 @class）都能正常读写。
 */
@Service
public class CartServiceImpl {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private String key(Long userId) { return "cart:" + userId; }

    /**
     * 从JSON字符串反序列化为Map。
     * 兼容 GenericJackson2JsonRedisSerializer 产生的旧数据格式:
     * - {"@class":"java.util.HashMap","skuId":["java.lang.Long",5],...}
     * - ["java.util.HashMap", {"skuId":5,...}]
     * 自动解包 Jackson 类型包装数组，并去除 @class 元数据字段。
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> deserialize(String json) {
        if (json == null) return null;
        try {
            Object raw = objectMapper.readValue(json, Object.class);
            // 顶层 Jackson 类型包装: ["java.util.HashMap", {...}]
            raw = unwrap(raw);
            if (!(raw instanceof Map)) {
                throw new BusinessException("购物车数据格式异常");
            }
            return cleanMap((Map<String, Object>) raw);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("购物车数据读取失败");
        }
    }

    /** 递归解包 Jackson 类型包装: ["java.lang.Long", 5] -> 5 */
    private Object unwrap(Object value) {
        if (value instanceof List<?> list && list.size() == 2 && list.get(0) instanceof String) {
            return unwrap(list.get(1));
        }
        return value;
    }

    /** 去除 @class 元数据，递归解包 map 中所有值的类型包装 */
    @SuppressWarnings("unchecked")
    private Map<String, Object> cleanMap(Map<String, Object> map) {
        Map<String, Object> cleaned = new HashMap<>();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if ("@class".equals(e.getKey())) continue; // 旧 Jackson 类型元数据
            cleaned.put(e.getKey(), unwrap(e.getValue()));
        }
        return cleaned;
    }

    /** 将Map序列化为JSON字符串 */
    private String serialize(Map<String, Object> item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (Exception e) {
            throw new BusinessException("购物车数据存储失败");
        }
    }

    /** 加入购物车 */
    public void addItem(Long userId, Long skuId, String productName,
                        String specInfo, Integer price, Integer quantity, String image) {
        String k = key(userId);
        String field = skuId.toString();
        String existingJson = (String) stringRedisTemplate.opsForHash().get(k, field);
        if (existingJson != null) {
            // 已存在，数量累加
            Map<String, Object> item = deserialize(existingJson);
            Object qty = item.get("quantity");
            int currentQty = qty instanceof Integer ? (Integer) qty : ((Number) qty).intValue();
            item.put("quantity", currentQty + quantity);
            stringRedisTemplate.opsForHash().put(k, field, serialize(item));
        } else {
            Map<String, Object> item = new HashMap<>();
            item.put("skuId", skuId);
            item.put("productName", productName);
            item.put("specInfo", specInfo);
            item.put("price", price);
            item.put("quantity", quantity);
            item.put("image", image);
            item.put("checked", true);
            stringRedisTemplate.opsForHash().put(k, field, serialize(item));
        }
    }

    /** 获取购物车 */
    public List<Map<String, Object>> list(Long userId) {
        List<Object> rawValues = stringRedisTemplate.opsForHash().values(key(userId));
        if (rawValues == null) return Collections.emptyList();
        return rawValues.stream()
                .map(v -> deserialize((String) v))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /** 修改数量 */
    public void updateQuantity(Long userId, Long skuId, Integer quantity) {
        String k = key(userId);
        String field = skuId.toString();
        String json = (String) stringRedisTemplate.opsForHash().get(k, field);
        if (json == null) throw new BusinessException("购物车无此商品");
        Map<String, Object> item = deserialize(json);
        if (quantity <= 0) {
            stringRedisTemplate.opsForHash().delete(k, field);
        } else {
            item.put("quantity", quantity);
            stringRedisTemplate.opsForHash().put(k, field, serialize(item));
        }
    }

    /** 删除 */
    public void removeItem(Long userId, Long skuId) {
        stringRedisTemplate.opsForHash().delete(key(userId), skuId.toString());
    }

    /** 勾选 */
    public void checkItem(Long userId, Long skuId, Boolean checked) {
        String k = key(userId);
        String field = skuId.toString();
        String json = (String) stringRedisTemplate.opsForHash().get(k, field);
        if (json != null) {
            Map<String, Object> item = deserialize(json);
            item.put("checked", checked);
            stringRedisTemplate.opsForHash().put(k, field, serialize(item));
        }
    }

    /** 获取选中商品 */
    public List<Map<String, Object>> getCheckedItems(Long userId) {
        return list(userId).stream()
                .filter(item -> {
                    Object checked = item.getOrDefault("checked", true);
                    if (checked instanceof Boolean) return (Boolean) checked;
                    return Boolean.TRUE.equals(checked);
                })
                .collect(Collectors.toList());
    }

    /** 清空已选 */
    public void removeChecked(Long userId) {
        String k = key(userId);
        List<Map<String, Object>> items = list(userId);
        for (Map<String, Object> item : items) {
            boolean checked = false;
            Object c = item.get("checked");
            if (c instanceof Boolean) checked = (Boolean) c;
            else if (c != null) checked = Boolean.TRUE.equals(c);
            if (checked) {
                stringRedisTemplate.opsForHash().delete(k, item.get("skuId").toString());
            }
        }
    }

    /** 清空购物车 */
    public void clear(Long userId) {
        stringRedisTemplate.delete(key(userId));
    }
}
