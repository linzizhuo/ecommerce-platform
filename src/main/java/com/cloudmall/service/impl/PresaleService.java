package com.cloudmall.service.impl;

import com.cloudmall.common.exception.BusinessException;
import com.cloudmall.entity.*;
import com.cloudmall.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预售服务 — 定金→尾款两阶段支付
 *
 * 流程:
 * 定金期 → 用户付定金 → 生成预售订单(状态0-待付尾款)
 * 尾款期 → 用户付尾款 → 订单转为正式(状态1-待发货)
 * 超时未付尾款 → 订单取消(定金不退)
 */
@Service
public class PresaleService {

    @Resource private PresaleMapper presaleMapper;
    @Resource private OrderMapper orderMapper;
    @Resource private SkuMapper skuMapper;
    @Resource private ActivityMapper activityMapper;
    @Resource private OrderItemMapper orderItemMapper;
    @Resource private ProductMapper productMapper;

    /** 查询有效预售活动 */
    public List<Presale> activeList() {
        LocalDateTime now = LocalDateTime.now();
        List<Presale> list = presaleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Presale>()
                        .le(Presale::getDepositStart, now)
                        .ge(Presale::getFinalEnd, now)
                        .orderByAsc(Presale::getDepositEnd));
        // 填充商品名称
        for (Presale presale : list) {
            Sku sku = skuMapper.selectById(presale.getSkuId());
            if (sku != null) {
                presale.setProductId(sku.getProductId());
                Product product = productMapper.selectById(sku.getProductId());
                presale.setProductName(product != null ? product.getName() : "未知商品");
            }
        }
        return list;
    }

    /** 支付定金 */
    @Transactional
    public Order payDeposit(Long userId, Long presaleId) {
        Presale presale = presaleMapper.selectById(presaleId);
        if (presale == null) throw new BusinessException("预售活动不存在");

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(presale.getDepositStart()) || now.isAfter(presale.getDepositEnd()))
            throw new BusinessException("不在定金支付期");

        Sku sku = skuMapper.selectById(presale.getSkuId());
        if (sku == null) throw new BusinessException("商品不存在");

        // 生成待付尾款订单
        Order order = new Order();
        order.setOrderNo("PRE" + System.currentTimeMillis());
        order.setUserId(userId);
        order.setTotalAmount(presale.getDeposit() + presale.getFinalAmount());
        order.setPayAmount(presale.getDeposit());  // 首付定金
        order.setStatus(0);  // 待付尾款
        order.setCreateTime(now);
        orderMapper.insert(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setSkuId(sku.getId());
        item.setProductName(sku.getProductId() != null ?
                (productMapper.selectById(sku.getProductId()) != null ?
                    productMapper.selectById(sku.getProductId()).getName() : "商品") : "商品");
        item.setPrice(presale.getDeposit() + presale.getFinalAmount());
        item.setQuantity(1);
        orderItemMapper.insert(item);

        return order;
    }

    /** 支付尾款 */
    @Transactional
    public Order payFinal(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId))
            throw new BusinessException("订单不存在");
        if (order.getStatus() != 0) throw new BusinessException("订单状态异常");

        // 检查尾款期
        List<OrderItem> items = orderItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId));
        if (items.isEmpty()) throw new BusinessException("订单异常");
        Long skuId = items.get(0).getSkuId();

        List<Presale> presales = presaleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Presale>()
                        .eq(Presale::getSkuId, skuId));
        if (presales.isEmpty()) throw new BusinessException("预售活动不存在");
        Presale presale = presales.get(0);

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(presale.getFinalStart()) || now.isAfter(presale.getFinalEnd()))
            throw new BusinessException("不在尾款支付期");

        order.setPayAmount(order.getTotalAmount()); // 补齐全款
        order.setStatus(1); // 已支付→待发货
        orderMapper.updateById(order);
        return order;
    }
}
