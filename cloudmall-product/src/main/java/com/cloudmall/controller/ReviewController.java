package com.cloudmall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.common.result.R;
import com.cloudmall.entity.Review;
import com.cloudmall.entity.Sku;
import com.cloudmall.mapper.ReviewMapper;
import com.cloudmall.mapper.SkuMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Resource
    private ReviewMapper reviewMapper;
    @Resource
    private SkuMapper skuMapper;

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
        // 参数校验
        Object productIdObj = body.get("productId");
        Object skuIdObj = body.get("skuId");
        Object ratingObj = body.get("rating");
        if (ratingObj == null) return R.fail(400, "请给出评分");

        // productId 优先，为空时从 skuId 推导
        Long productId;
        if (productIdObj != null) {
            productId = Long.valueOf(productIdObj.toString());
        } else if (skuIdObj != null) {
            Long skuId = Long.valueOf(skuIdObj.toString());
            Sku sku = skuMapper.selectById(skuId);
            if (sku == null) return R.fail(400, "SKU不存在");
            productId = sku.getProductId();
        } else {
            return R.fail(400, "请指定商品ID或SKUID");
        }

        Review r = new Review();
        r.setUserId(userId);
        r.setProductId(productId);
        r.setOrderId(body.get("orderId") != null ? Long.valueOf(body.get("orderId").toString()) : null);
        r.setRating(ratingObj instanceof Integer ? (Integer) ratingObj : Integer.valueOf(ratingObj.toString()));
        r.setContent((String) body.getOrDefault("content", ""));
        r.setCreateTime(LocalDateTime.now());
        reviewMapper.insert(r);
        return R.ok();
    }
}
