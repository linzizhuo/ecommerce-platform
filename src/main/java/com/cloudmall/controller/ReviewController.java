package com.cloudmall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.common.result.R;
import com.cloudmall.entity.Review;
import com.cloudmall.mapper.ReviewMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Resource
    private ReviewMapper reviewMapper;

    /** 商品评价列表 */
    @GetMapping("/product/{productId}")
    public R<Map<String, Object>> productReviews(@PathVariable Long productId) {
        List<Review> reviews = reviewMapper.selectList(
            new LambdaQueryWrapper<Review>().eq(Review::getProductId, productId)
                .orderByDesc(Review::getCreateTime)
        );
        double avgRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
        Map<String, Object> result = new HashMap<>();
        result.put("list", reviews);
        result.put("avgRating", Math.round(avgRating * 10) / 10.0);
        result.put("total", reviews.size());
        return R.ok(result);
    }

    /** 提交评价 */
    @PostMapping
    public R<Void> submit(@RequestAttribute("userId") Long userId,
                          @RequestBody Map<String, Object> body) {
        Review r = new Review();
        r.setUserId(userId);
        r.setProductId(Long.valueOf(body.get("productId").toString()));
        r.setOrderId(body.get("orderId") != null ? Long.valueOf(body.get("orderId").toString()) : null);
        r.setRating((Integer) body.get("rating"));
        r.setContent((String) body.getOrDefault("content", ""));
        r.setCreateTime(LocalDateTime.now());
        reviewMapper.insert(r);
        return R.ok();
    }
}
