package com.cloudmall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.common.result.R;
import com.cloudmall.entity.*;
import com.cloudmall.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Resource private UserMapper userMapper;
    @Resource private ProductMapper productMapper;
    @Resource private OrderMapper orderMapper;
    @Resource private PaymentMapper paymentMapper;
    @Resource private ReviewMapper reviewMapper;

    // ===== 数据大屏 =====
    @GetMapping("/dashboard")
    public R<Map<String, Object>> dashboard() {
        List<User> users = userMapper.selectList(null);
        List<Product> products = productMapper.selectList(null);
        List<Order> orders = orderMapper.selectList(null);
        int revenue = orders.stream().filter(o -> o.getStatus() != null && o.getStatus() >= 1)
            .mapToInt(o -> o.getPayAmount() != null ? o.getPayAmount() : 0).sum();

        Map<String, Object> data = new HashMap<>();
        data.put("userCount", users.size());
        data.put("productCount", products.size());
        data.put("orderCount", orders.size());
        data.put("revenue", revenue);
        return R.ok(data);
    }

    // ===== 用户管理 =====
    @GetMapping("/users")
    public R<List<Map<String, Object>>> users() {
        List<User> users = userMapper.selectList(null);
        return R.ok(users.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId()); m.put("phone", u.getPhone());
            m.put("nickname", u.getNickname()); m.put("status", u.getStatus());
            m.put("createTime", u.getCreateTime());
            return m;
        }).collect(Collectors.toList()));
    }

    @PutMapping("/user/{id}/status")
    public R<Void> userStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        User u = userMapper.selectById(id);
        if (u != null) { u.setStatus((Integer) body.get("status")); userMapper.updateById(u); }
        return R.ok();
    }

    // ===== 商品审核 =====
    @GetMapping("/products")
    public R<List<Product>> products() {
        return R.ok(productMapper.selectList(
            new LambdaQueryWrapper<Product>().orderByDesc(Product::getCreateTime)));
    }

    @PutMapping("/product/{id}/status")
    public R<Void> auditProduct(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Product p = productMapper.selectById(id);
        if (p != null) { p.setStatus((Integer) body.get("status")); productMapper.updateById(p); }
        return R.ok();
    }

    // ===== 订单总览 =====
    @GetMapping("/orders")
    public R<List<Order>> orders() {
        return R.ok(orderMapper.selectList(
            new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreateTime)));
    }

    // ===== 评价管理 =====
    @GetMapping("/reviews")
    public R<List<Review>> reviews() {
        return R.ok(reviewMapper.selectList(
            new LambdaQueryWrapper<Review>().orderByDesc(Review::getCreateTime)));
    }

    @DeleteMapping("/review/{id}")
    public R<Void> deleteReview(@PathVariable Long id) {
        reviewMapper.deleteById(id);
        return R.ok();
    }
}
