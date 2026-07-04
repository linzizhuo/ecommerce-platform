package com.cloudmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.common.annotation.RateLimit;
import com.cloudmall.common.exception.BusinessException;
import com.cloudmall.entity.*;
import com.cloudmall.mapper.*;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 订单服务 — 高并发下单核心流程
 *
 * 高并发设计要点:
 * 1. 分布式锁批量加锁（按skuId排序防死锁）
 * 2. MySQL原子UPDATE兜底（WHERE stock >= quantity）
 * 3. 锁在finally块释放（保证异常也能释放）
 * 4. 限流注解保护（防止恶意刷单）
 * 5. 非核心逻辑异步化（发通知等不影响主流程）
 * 6. 满减活动自动匹配叠加
 */
@Service
public class OrderServiceImpl {

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderItemMapper orderItemMapper;
    @Resource
    private PaymentMapper paymentMapper;
    @Resource
    private SkuMapper skuMapper;
    @Resource
    private CouponTemplateMapper couponTemplateMapper;
    @Resource
    private UserCouponMapper userCouponMapper;
    @Resource
    private OrderLogMapper orderLogMapper;
    @Resource
    private StockLogMapper stockLogMapper;
    @Resource
    private ActivityMapper activityMapper;
    @Resource
    private ActivityProductMapper activityProductMapper;
    @Resource
    private CartServiceImpl cartService;
    @Resource
    private LogisticsMapper logisticsMapper;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private OrderAsyncService orderAsyncService;

    /**
     * 下单核心流程（加限流保护）
     */
    @RateLimit(qps = 50, timeout = 2)
    @Transactional
    public Map<String, Object> create(Long userId, Long addressId, Long couponId,
                                       String addressSnapshot, Integer payMethod) {
        // 1. 获取购物车选中商品
        List<Map<String, Object>> cartItems = cartService.getCheckedItems(userId);
        if (cartItems.isEmpty()) throw new BusinessException("请选择商品");

        // 2. 按skuId排序 → 所有线程的加锁顺序一致 → 防死锁
        cartItems.sort(Comparator.comparing(
                item -> Long.valueOf(item.get("skuId").toString())));

        int totalAmount = cartItems.stream()
                .mapToInt(item -> (Integer) item.get("price") * (Integer) item.get("quantity"))
                .sum();

        // 提取商品ID列表用于满减匹配
        List<Long> productIds = cartItems.stream()
                .map(item -> Long.valueOf(item.get("productId").toString()))
                .distinct().toList();

        int couponDiscount = calculateCouponDiscount(couponId, userId, totalAmount);
        int fullReductionDiscount = calculateFullReduction(totalAmount, productIds);
        int discountAmount = couponDiscount + fullReductionDiscount;
        int payAmount = totalAmount - discountAmount;

        // 3. 批量获取分布式锁 + 原子扣库存
        List<RLock> acquiredLocks = new ArrayList<>();
        try {
            for (Map<String, Object> item : cartItems) {
                Long skuId = Long.valueOf(item.get("skuId").toString());
                int quantity = (Integer) item.get("quantity");

                RLock lock = redissonClient.getLock("stock:" + skuId);
                if (!lock.tryLock(3, 5, TimeUnit.SECONDS)) {
                    throw new BusinessException("商品[" + item.get("productName") + "]抢购繁忙，请重试");
                }
                acquiredLocks.add(lock);
            }

            // 4. 原子扣库存
            for (int i = 0; i < cartItems.size(); i++) {
                Map<String, Object> item = cartItems.get(i);
                Long skuId = Long.valueOf(item.get("skuId").toString());
                int quantity = (Integer) item.get("quantity");

                int rows = skuMapper.deductStock(skuId, quantity);
                if (rows == 0) {
                    throw new BusinessException("商品[" + item.get("productName") + "]库存不足");
                }
                // 记录库存流水
                StockLog sl = new StockLog();
                sl.setSkuId(skuId);
                sl.setChangeCount(-quantity);
                sl.setType(1); // 下单扣减
                sl.setCreateTime(LocalDateTime.now());
                stockLogMapper.insert(sl);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统繁忙");
        } finally {
            releaseLocks(acquiredLocks);
        }

        // 5. 生成订单
        Order order = buildOrder(userId, totalAmount, discountAmount, payAmount, couponId, addressSnapshot);
        orderMapper.insert(order);

        // 6. 订单明细
        for (Map<String, Object> item : cartItems) {
            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setSkuId(Long.valueOf(item.get("skuId").toString()));
            oi.setProductName((String) item.get("productName"));
            oi.setSpecInfo((String) item.get("specInfo"));
            oi.setPrice((Integer) item.get("price"));
            oi.setQuantity((Integer) item.get("quantity"));
            oi.setProductImage((String) item.get("image"));
            orderItemMapper.insert(oi);
        }

        // 7. 核销优惠券
        if (couponId != null && couponId > 0) {
            consumeCoupon(couponId);
        }

        // 8. 清空购物车已选
        cartService.removeChecked(userId);

        // 9. 订单日志
        logOrderStatus(order.getId(), userId, 1, "用户下单", null, 0, "满减:" + fullReductionDiscount + " 优惠券:" + couponDiscount);

        // 10. 异步：失效缓存
        orderAsyncService.invalidateProductCaches(cartItems);

        // 11. 异步：发送通知
        orderAsyncService.sendOrderNotification(order);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("payAmount", payAmount);
        result.put("totalAmount", totalAmount);
        result.put("discountAmount", discountAmount);
        result.put("couponDiscount", couponDiscount);
        result.put("fullReductionDiscount", fullReductionDiscount);
        return result;
    }

    /** 模拟支付 */
    @Transactional
    public void pay(Long orderId, Long userId, Integer payMethod) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) throw new BusinessException("订单不存在");
        if (order.getStatus() != 0) throw new BusinessException("订单状态异常");

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPayNo("PAY" + System.currentTimeMillis());
        payment.setAmount(order.getPayAmount());
        payment.setPayMethod(payMethod);
        payment.setStatus(1);
        payment.setPayTime(LocalDateTime.now());
        paymentMapper.insert(payment);

        int oldStatus = order.getStatus();
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        logOrderStatus(orderId, userId, 1, "用户支付", oldStatus, 1, "支付方式:" + payMethod);
    }

    /** 支付回调处理（供PaymentController调用） */
    @Transactional
    public void handlePaymentCallback(String orderNo, String payNo, Integer amount,
                                       Integer payMethod, Long operatorId) {
        Order order = orderMapper.selectOne(
            new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) throw new BusinessException("订单不存在");
        if (order.getStatus() != 0) {
            // 幂等：已支付则直接返回
            if (order.getStatus() == 1) return;
            throw new BusinessException("订单状态异常");
        }

        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setPayNo(payNo);
        payment.setAmount(amount != null ? amount : order.getPayAmount());
        payment.setPayMethod(payMethod != null ? payMethod : 1);
        payment.setStatus(1);
        payment.setPayTime(LocalDateTime.now());
        paymentMapper.insert(payment);

        int oldStatus = order.getStatus();
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        logOrderStatus(order.getId(), operatorId, 3, "支付回调", oldStatus, 1, "支付单号:" + payNo);
    }

    /** 发货 */
    @Transactional
    public void ship(Long orderId, String company, String trackingNo) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 1) throw new BusinessException("订单状态异常");
        int oldStatus = order.getStatus();
        order.setStatus(2);
        orderMapper.updateById(order);

        logOrderStatus(orderId, order.getUserId(), 2, "商家发货", oldStatus, 2,
            "快递:" + company + " 单号:" + trackingNo);
    }

    /** 确认收货 */
    @Transactional
    public void confirm(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) throw new BusinessException("订单不存在");
        if (order.getStatus() != 2) throw new BusinessException("订单状态异常");
        int oldStatus = order.getStatus();
        order.setStatus(3);
        orderMapper.updateById(order);

        logOrderStatus(orderId, userId, 1, "确认收货", oldStatus, 3, null);
    }

    /** 取消订单 + 恢复库存 */
    @Transactional
    public void cancel(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) throw new BusinessException("订单不存在");
        if (order.getStatus() != 0) throw new BusinessException("仅待支付订单可取消");

        int oldStatus = order.getStatus();
        order.setStatus(4);
        orderMapper.updateById(order);

        // 恢复库存
        List<OrderItem> items = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        for (OrderItem item : items) {
            skuMapper.restoreStock(item.getSkuId(), item.getQuantity());
            // 库存流水
            StockLog sl = new StockLog();
            sl.setSkuId(item.getSkuId());
            sl.setOrderId(orderId);
            sl.setChangeCount(item.getQuantity());
            sl.setType(3); // 取消回滚
            sl.setCreateTime(LocalDateTime.now());
            stockLogMapper.insert(sl);
        }

        // 恢复优惠券
        if (order.getCouponId() != null && order.getCouponId() > 0) {
            restoreCoupon(order.getCouponId());
        }

        logOrderStatus(orderId, userId, 1, "取消订单", oldStatus, 4, null);
    }

    /** 商家改单 */
    @Transactional
    public void modify(Long orderId, Map<String, Object> changes) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (order.getStatus() != 0) throw new BusinessException("仅待支付订单可修改");

        boolean changed = false;
        StringBuilder remark = new StringBuilder();

        // 修改地址
        if (changes.containsKey("addressSnapshot")) {
            order.setAddressSnapshot((String) changes.get("addressSnapshot"));
            remark.append("地址已更新; ");
            changed = true;
        }

        // 修改商品项
        if (changes.containsKey("items")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> newItems = (List<Map<String, Object>>) changes.get("items");
            // 恢复旧库存
            List<OrderItem> oldItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
            for (OrderItem oi : oldItems) {
                skuMapper.restoreStock(oi.getSkuId(), oi.getQuantity());
                StockLog sl = new StockLog();
                sl.setSkuId(oi.getSkuId()); sl.setOrderId(orderId);
                sl.setChangeCount(oi.getQuantity()); sl.setType(3);
                sl.setCreateTime(LocalDateTime.now());
                stockLogMapper.insert(sl);
            }
            // 删除旧明细
            orderItemMapper.delete(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
            // 扣新库存 + 插入新明细
            int newTotal = 0;
            for (Map<String, Object> ni : newItems) {
                Long skuId = Long.valueOf(ni.get("skuId").toString());
                int qty = (Integer) ni.get("quantity");
                int price = (Integer) ni.get("price");

                int rows = skuMapper.deductStock(skuId, qty);
                if (rows == 0) throw new BusinessException("SKU[" + skuId + "]库存不足");

                StockLog sl = new StockLog();
                sl.setSkuId(skuId); sl.setOrderId(orderId);
                sl.setChangeCount(-qty); sl.setType(1);
                sl.setCreateTime(LocalDateTime.now());
                stockLogMapper.insert(sl);

                OrderItem oi = new OrderItem();
                oi.setOrderId(orderId); oi.setSkuId(skuId);
                oi.setProductName((String) ni.getOrDefault("productName", ""));
                oi.setSpecInfo((String) ni.getOrDefault("specInfo", ""));
                oi.setPrice(price); oi.setQuantity(qty);
                oi.setProductImage((String) ni.getOrDefault("image", ""));
                orderItemMapper.insert(oi);

                newTotal += price * qty;
            }
            int newDiscount = calculateCouponDiscount(order.getCouponId(), order.getUserId(), newTotal);
            int newPay = newTotal - newDiscount;
            order.setTotalAmount(newTotal);
            order.setDiscountAmount(newDiscount);
            order.setPayAmount(newPay);
            remark.append("商品项已更新; ");
            changed = true;
        }

        if (changed) {
            orderMapper.updateById(order);
            logOrderStatus(orderId, 0L, 2, "商家改单", 0, 0, remark.toString().trim());
        }
    }

    /** 用户订单列表 */
    public List<Order> userOrders(Long userId, Integer status) {
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime);
        if (status != null) qw.eq(Order::getStatus, status);
        return orderMapper.selectList(qw);
    }

    /** 订单详情（含物流和支付信息） */
    public Map<String, Object> detail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        // 查询物流信息
        Logistics logistics = logisticsMapper.selectOne(
                new LambdaQueryWrapper<Logistics>().eq(Logistics::getOrderId, orderId));
        // 查询支付信息
        List<Payment> payments = paymentMapper.selectList(
                new LambdaQueryWrapper<Payment>().eq(Payment::getOrderId, orderId));
        Payment payment = payments.isEmpty() ? null : payments.get(0);

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("items", items);
        result.put("logistics", logistics);
        result.put("payment", payment);
        return result;
    }

    // ==================== 私有辅助方法 ====================

    /** 优惠券计算 */
    private int calculateCouponDiscount(Long couponId, Long userId, int totalAmount) {
        if (couponId == null || couponId <= 0) return 0;
        UserCoupon uc = userCouponMapper.selectById(couponId);
        if (uc == null || !uc.getUserId().equals(userId) || uc.getStatus() != 0)
            throw new BusinessException("优惠券不可用");
        CouponTemplate ct = couponTemplateMapper.selectById(uc.getTemplateId());
        if (ct == null || totalAmount < ct.getThreshold()) return 0;

        int discount;
        if (ct.getType() != null && ct.getType() == 2) {
            discount = totalAmount * (100 - ct.getValue()) / 100;
        } else {
            discount = ct.getValue();
        }
        return Math.max(0, Math.min(discount, totalAmount));
    }

    /** 满减活动匹配引擎 */
    private int calculateFullReduction(int totalAmount, List<Long> productIds) {
        if (productIds.isEmpty()) return 0;

        // 查所有进行中的满减活动
        LocalDateTime now = LocalDateTime.now();
        List<Activity> activities = activityMapper.selectList(
            new LambdaQueryWrapper<Activity>()
                .eq(Activity::getType, 5)  // type=5 满减
                .eq(Activity::getStatus, 2) // 进行中
                .le(Activity::getStartTime, now)
                .ge(Activity::getEndTime, now));

        if (activities.isEmpty()) return 0;

        int maxReduction = 0;
        for (Activity act : activities) {
            // 检查是否有商品参与此活动
            List<ActivityProduct> apList = activityProductMapper.selectList(
                new LambdaQueryWrapper<ActivityProduct>()
                    .eq(ActivityProduct::getActivityId, act.getId()));
            Set<Long> activityProductIds = new HashSet<>();
            for (ActivityProduct ap : apList) {
                activityProductIds.add(ap.getProductId());
            }
            // 至少有一个购物车商品参与此活动
            boolean matched = productIds.stream().anyMatch(activityProductIds::contains);
            if (!matched) continue;

            // 解析 rules JSON: {"threshold":20000,"reduce":3000} 或 {"tiers":[{"threshold":20000,"reduce":3000},{"threshold":30000,"reduce":5000}]}
            try {
                String rules = act.getRules();
                if (rules == null || rules.isEmpty()) continue;

                // 支持多档满减
                if (rules.contains("\"tiers\"")) {
                    // 多档模式
                    String tiersStr = rules.replaceAll(".*\"tiers\"\\s*:\\s*\\[", "").replaceAll("\\].*", "");
                    String[] tierParts = tiersStr.split("\\},\\s*\\{");
                    for (String tier : tierParts) {
                        int threshold = extractJsonInt(tier, "threshold");
                        int reduce = extractJsonInt(tier, "reduce");
                        if (totalAmount >= threshold && reduce > maxReduction) {
                            maxReduction = reduce;
                        }
                    }
                } else {
                    // 单档模式
                    int threshold = extractJsonInt(rules, "threshold");
                    int reduce = extractJsonInt(rules, "reduce");
                    if (totalAmount >= threshold && reduce > maxReduction) {
                        maxReduction = reduce;
                    }
                }
            } catch (Exception e) {
                // 解析失败跳过
            }
        }
        return Math.min(maxReduction, totalAmount);
    }

    /** 从JSON字符串提取int值 */
    private int extractJsonInt(String json, String key) {
        try {
            String[] parts = json.split("\"" + key + "\"\\s*:\\s*");
            if (parts.length < 2) return 0;
            String val = parts[1].split("[,\\}]")[0].trim();
            return Integer.parseInt(val);
        } catch (Exception e) {
            return 0;
        }
    }

    /** 核销优惠券 */
    private void consumeCoupon(Long couponId) {
        UserCoupon uc = userCouponMapper.selectById(couponId);
        if (uc != null) {
            uc.setStatus(1);
            userCouponMapper.updateById(uc);
        }
    }

    /** 恢复优惠券 */
    private void restoreCoupon(Long couponId) {
        UserCoupon uc = userCouponMapper.selectById(couponId);
        if (uc != null && uc.getStatus() == 1) {
            uc.setStatus(0);
            userCouponMapper.updateById(uc);
        }
    }

    /** 写入订单操作日志 */
    private void logOrderStatus(Long orderId, Long operatorId, Integer operatorType,
                                 String action, Integer oldStatus, Integer newStatus, String remark) {
        OrderLog log = new OrderLog();
        log.setOrderId(orderId);
        log.setOperatorId(operatorId);
        log.setOperatorType(operatorType);
        log.setAction(action);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        orderLogMapper.insert(log);
    }

    /** 释放所有锁 */
    private void releaseLocks(List<RLock> locks) {
        for (RLock lock : locks) {
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception ignored) {}
        }
    }

    /** 构建订单对象 */
    private Order buildOrder(Long userId, int totalAmount, int discountAmount,
                              int payAmount, Long couponId, String addressSnapshot) {
        Order order = new Order();
        order.setOrderNo("OM" + System.currentTimeMillis()
                + String.format("%04d", new Random().nextInt(10000)));
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setPayAmount(payAmount);
        order.setStatus(0);
        order.setCouponId(couponId);
        order.setAddressSnapshot(addressSnapshot);
        order.setCreateTime(LocalDateTime.now());
        return order;
    }
}
