package com.cloudmall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.common.result.R;
import com.cloudmall.entity.*;
import com.cloudmall.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    @Resource private ActivityMapper activityMapper;
    @Resource private ActivityProductMapper activityProductMapper;
    @Resource private ViolationMapper violationMapper;

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

    // ===== 活动管理 =====
    @GetMapping("/activities")
    public R<List<Map<String, Object>>> activities(@RequestParam(required = false) Integer type) {
        LambdaQueryWrapper<Activity> qw = new LambdaQueryWrapper<Activity>()
                .orderByDesc(Activity::getCreateTime);
        if (type != null) qw.eq(Activity::getType, type);
        List<Activity> list = activityMapper.selectList(qw);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Activity a : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("name", a.getName());
            m.put("type", a.getType());
            m.put("rules", a.getRules());
            m.put("startTime", a.getStartTime());
            m.put("endTime", a.getEndTime());
            m.put("status", a.getStatus());
            m.put("merchantId", a.getMerchantId());
            m.put("createTime", a.getCreateTime());
            result.add(m);
        }
        return R.ok(result);
    }

    @PostMapping("/activity")
    public R<?> createActivity(@RequestBody Map<String, Object> body) {
        LocalDateTime startTime = LocalDateTime.parse((String) body.get("startTime"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime endTime = LocalDateTime.parse((String) body.get("endTime"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Integer type = (Integer) body.get("type");

        // 排期冲突检测
        List<Map<String, String>> conflicts = checkScheduleConflict(type, startTime, endTime, null);
        if (!conflicts.isEmpty()) {
            Map<String, Object> warn = new HashMap<>();
            warn.put("warning", "存在时间重叠的活动");
            warn.put("conflicts", conflicts);
            return R.ok(warn);
        }

        Activity a = new Activity();
        a.setName((String) body.get("name"));
        a.setType(type);
        a.setRules((String) body.get("rules"));
        a.setStartTime(startTime);
        a.setEndTime(endTime);
        a.setStatus(1); // 待审核
        a.setCreateTime(LocalDateTime.now());
        activityMapper.insert(a);
        return R.ok();
    }

    @PutMapping("/activity/{id}/status")
    public R<?> auditActivity(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Activity a = activityMapper.selectById(id);
        if (a != null) {
            Integer newStatus = (Integer) body.get("status");
            // 审核通过时检测排期冲突
            if (newStatus == 2) {
                List<Map<String, String>> conflicts = checkScheduleConflict(
                        a.getType(), a.getStartTime(), a.getEndTime(), id);
                if (!conflicts.isEmpty()) {
                    Map<String, Object> warn = new HashMap<>();
                    warn.put("warning", "存在时间重叠的进行中活动");
                    warn.put("conflicts", conflicts);
                    return R.ok(warn);
                }
            }
            a.setStatus(newStatus);
            activityMapper.updateById(a);
        }
        return R.ok();
    }

    /** 检测活动排期冲突 */
    private List<Map<String, String>> checkScheduleConflict(Integer type,
            LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        List<Map<String, String>> conflicts = new ArrayList<>();
        LambdaQueryWrapper<Activity> qw = new LambdaQueryWrapper<Activity>()
                .eq(Activity::getType, type)
                .eq(Activity::getStatus, 2) // 进行中的活动
                .lt(Activity::getStartTime, endTime)
                .gt(Activity::getEndTime, startTime);
        if (excludeId != null) qw.ne(Activity::getId, excludeId);
        List<Activity> overlapping = activityMapper.selectList(qw);
        for (Activity act : overlapping) {
            Map<String, String> info = new HashMap<>();
            info.put("id", act.getId().toString());
            info.put("name", act.getName());
            info.put("startTime", act.getStartTime().toString());
            info.put("endTime", act.getEndTime().toString());
            conflicts.add(info);
        }
        return conflicts;
    }

    // ===== 违规处罚 =====
    @GetMapping("/violations")
    public R<List<Violation>> violations() {
        return R.ok(violationMapper.selectList(
            new LambdaQueryWrapper<Violation>().orderByDesc(Violation::getCreateTime)));
    }

    // ===== 对账报表 =====
    @GetMapping("/reconciliation")
    public R<List<Map<String, Object>>> reconciliation(
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<Order>()
                .ge(Order::getStatus, 1)
                .orderByDesc(Order::getCreateTime);

        if (startDate != null && !startDate.isEmpty()) {
            qw.ge(Order::getPayTime, LocalDateTime.parse(startDate + "T00:00:00"));
        }
        if (endDate != null && !endDate.isEmpty()) {
            qw.le(Order::getPayTime, LocalDateTime.parse(endDate + "T23:59:59"));
        }

        List<Order> orders = orderMapper.selectList(qw);
        // 按日期分组
        Map<String, Map<String, Object>> dailyMap = new LinkedHashMap<>();
        for (Order o : orders) {
            String dateKey = o.getPayTime() != null ?
                    o.getPayTime().toLocalDate().toString() :
                    o.getCreateTime().toLocalDate().toString();
            dailyMap.computeIfAbsent(dateKey, k -> {
                Map<String, Object> m = new HashMap<>();
                m.put("date", k);
                m.put("orderCount", 0);
                m.put("totalAmount", 0);
                return m;
            });
            Map<String, Object> d = dailyMap.get(dateKey);
            d.put("orderCount", (Integer) d.get("orderCount") + 1);
            d.put("totalAmount", (Integer) d.get("totalAmount") +
                    (o.getPayAmount() != null ? o.getPayAmount() : 0));
        }
        return R.ok(new ArrayList<>(dailyMap.values()));
    }
}
