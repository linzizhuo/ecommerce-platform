package com.cloudmall.service.impl;

import com.cloudmall.common.exception.BusinessException;
import com.cloudmall.entity.*;
import com.cloudmall.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 搜索服务 — Redis热词联想 + MySQL全文搜索
 */
@Service
public class SearchService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private SearchHotWordMapper hotWordMapper;
    @Resource
    private ProductMapper productMapper;

    private static final String HOT_WORDS_KEY = "search:hotwords";

    /** 搜索联想 — Redis ZSET按搜索频次排序，返回Top10 */
    public List<Map<String, Object>> suggest(String keyword) {
        // 记录搜索词，score+1
        redisTemplate.opsForZSet().incrementScore(HOT_WORDS_KEY, keyword, 1);

        // 模糊匹配联想词
        Set<ZSetOperations.TypedTuple<Object>> range =
                redisTemplate.opsForZSet().reverseRangeByScoreWithScores(HOT_WORDS_KEY, 0, Double.MAX_VALUE, 0, 10);

        List<Map<String, Object>> result = new ArrayList<>();
        if (range != null) {
            for (ZSetOperations.TypedTuple<Object> t : range) {
                String word = (String) t.getValue();
                if (word != null && word.contains(keyword)) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("keyword", word);
                    m.put("count", t.getScore().longValue());
                    result.add(m);
                }
            }
        }
        return result;
    }

    /** 搜索商品 — 走MySQL，结果缓存5分钟 */
    public List<Product> search(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return Collections.emptyList();
        }
        String cacheKey = "search:result:" + keyword;
        @SuppressWarnings("unchecked")
        List<Product> cached = (List<Product>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) return cached;

        List<Product> list = productMapper.search(keyword);
        redisTemplate.opsForValue().set(cacheKey, (Object) list, Duration.ofMinutes(5));
        return list;
    }

    /** 获取热门搜索词 */
    public List<Map<String, Object>> hotWords() {
        Set<ZSetOperations.TypedTuple<Object>> top =
                redisTemplate.opsForZSet().reverseRangeWithScores(HOT_WORDS_KEY, 0, 9);
        List<Map<String, Object>> result = new ArrayList<>();
        if (top != null) {
            for (ZSetOperations.TypedTuple<Object> t : top) {
                Map<String, Object> m = new HashMap<>();
                m.put("keyword", t.getValue());
                m.put("count", t.getScore().longValue());
                result.add(m);
            }
        }
        return result;
    }
}
