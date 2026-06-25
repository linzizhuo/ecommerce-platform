package com.cloudmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
    private CartServiceImpl cartService;
    @Resource
    private RedissonClient redissonClient;

    /**
     * 下单核心流程: 校验→锁库存→生成订单→核销优惠券→清购物车
     */
    @Transactional
    public Map<String, Object> create(Long userId, Long addressId, Long couponId,
                                       String addressSnapshot, Integer payMethod) {
        // 1. 获取购物车选中商品
        List<Map<String, Object>> cartItems = cartService.getCheckedItems(userId);
        if (cartItems.isEmpty()) throw new BusinessException("请选择商品");

        int totalAmount = cartItems.stream()
                .mapToInt(item -> (Integer) item.get("price") * (Integer) item.get("quantity"))
                .sum();
        int discountAmount = 0;

        // 2. 优惠券计算
        if (couponId != null && couponId > 0) {
            UserCoupon uc = userCouponMapper.selectById(couponId);
            if (uc == null || !uc.getUserId().equals(userId) || uc.getStatus() != 0)
                throw new BusinessException("优惠券不可用");
            CouponTemplate ct = couponTemplateMapper.selectById(uc.getTemplateId());
            if (ct != null && totalAmount >= ct.getThreshold()) {
                if (ct.getType() != null && ct.getType() == 2) {
                    // 折扣券: value=85表示8.5折, 优惠=总价*(100-value)/100
                    discountAmount = totalAmount * (100 - ct.getValue()) / 100;
                } else {
                    // 满减券: value=减免金额
                    discountAmount = ct.getValue();
                }
                if (discountAmount < 0) discountAmount = 0;
                if (discountAmount > totalAmount) discountAmount = totalAmount;
            }
        }

        int payAmount = totalAmount - discountAmount;

        // 3. 库存扣减 (Redisson分布式锁防超卖)
        for (Map<String, Object> item : cartItems) {
            Long skuId = Long.valueOf(item.get("skuId").toString());
            int quantity = (Integer) item.get("quantity");
            RLock lock = redissonClient.getLock("stock:" + skuId);
            try {
                if (lock.tryLock(3, 5, TimeUnit.SECONDS)) {
                    Sku sku = skuMapper.selectById(skuId);
                    if (sku == null || sku.getStock() < quantity) {
                        throw new BusinessException("商品[" + item.get("productName") + "]库存不足");
                    }
                    sku.setStock(sku.getStock() - quantity);
                    skuMapper.updateById(sku);
                }
            } catch (InterruptedException e) {
                throw new BusinessException("系统繁忙");
            } finally {
                if (lock.isHeldByCurrentThread()) lock.unlock();
            }
        }

        // 4. 生成订单
        Order order = new Order();
        order.setOrderNo("OM" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000)));
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setPayAmount(payAmount);
        order.setStatus(0);
        order.setCouponId(couponId);
        order.setAddressSnapshot(addressSnapshot);
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);

        // 5. 订单明细
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

        // 6. 核销优惠券
        if (couponId != null && couponId > 0) {
            UserCoupon uc = userCouponMapper.selectById(couponId);
            uc.setStatus(1);
            userCouponMapper.updateById(uc);
        }

        // 7. 清空购物车已选
        cartService.removeChecked(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("payAmount", payAmount);
        return result;
    }

    /** 模拟支付 */
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

        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    /** 发货 */
    public void ship(Long orderId, String company, String trackingNo) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 1) throw new BusinessException("订单状态异常");
        order.setStatus(2);
        orderMapper.updateById(order);
    }

    /** 确认收货 */
    public void confirm(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) throw new BusinessException("订单不存在");
        if (order.getStatus() != 2) throw new BusinessException("订单状态异常");
        order.setStatus(3);
        orderMapper.updateById(order);
    }

    /** 用户订单列表 */
    public List<Order> userOrders(Long userId, Integer status) {
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime);
        if (status != null) qw.eq(Order::getStatus, status);
        return orderMapper.selectList(qw);
    }

    /** 订单详情 */
    public Map<String, Object> detail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("items", items);
        return result;
    }
}
