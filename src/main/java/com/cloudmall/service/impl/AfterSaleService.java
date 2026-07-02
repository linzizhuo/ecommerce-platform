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
 * 售后服务 — 退款/退货/换货
 */
@Service
public class AfterSaleService {

    @Resource
    private AfterSaleMapper afterSaleMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderLogMapper orderLogMapper;
    @Resource
    private PaymentMapper paymentMapper;
    @Resource
    private SkuMapper skuMapper;
    @Resource
    private OrderItemMapper orderItemMapper;

    /** 用户申请售后 */
    @Transactional
    public AfterSale apply(Long userId, Long orderId, Integer type, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId))
            throw new BusinessException("订单不存在");
        if (order.getStatus() < 1 || order.getStatus() > 3)
            throw new BusinessException("订单状态不支持售后");

        // 检查是否已有售后单
        List<AfterSale> exist = afterSaleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AfterSale>()
                        .eq(AfterSale::getOrderId, orderId)
                        .eq(AfterSale::getUserId, userId));
        if (!exist.isEmpty()) throw new BusinessException("已提交过售后申请");

        AfterSale as = new AfterSale();
        as.setOrderId(orderId);
        as.setUserId(userId);
        as.setType(type);
        as.setReason(reason);
        as.setAmount(order.getPayAmount());
        as.setStatus(0);
        afterSaleMapper.insert(as);

        // 记录日志
        logOrder(orderId, userId, 1, "申请售后: type=" + type + " reason=" + reason, order.getStatus(), order.getStatus());
        return as;
    }

    /** 商家审核 */
    @Transactional
    public void audit(Long merchantId, Long afterSaleId, Integer result, String remark) {
        AfterSale as = afterSaleMapper.selectById(afterSaleId);
        if (as == null) throw new BusinessException("售后单不存在");

        Order order = orderMapper.selectById(as.getOrderId());
        if (order == null) throw new BusinessException("订单不存在");

        if (result == 1) { // 同意
            as.setStatus(1);
            // 退款退货: 恢复库存
            if (as.getType() == 1 || as.getType() == 2) {
                order.setStatus(as.getType() == 2 ? 6 : 6); // 已退款
                orderMapper.updateById(order);
                // 恢复库存（简化处理）
                List<OrderItem> items = orderItemMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem>()
                                .eq(OrderItem::getOrderId, as.getOrderId()));
            }
            as.setStatus(1);
        } else {
            as.setStatus(2); // 驳回
        }
        afterSaleMapper.updateById(as);
        logOrder(as.getOrderId(), merchantId, 2, "审核售后: result=" + result, order.getStatus(), order.getStatus());
    }

    /** 用户售后列表 */
    public List<AfterSale> userList(Long userId) {
        return afterSaleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AfterSale>()
                        .eq(AfterSale::getUserId, userId)
                        .orderByDesc(AfterSale::getCreateTime));
    }

    private void logOrder(Long orderId, Long operatorId, Integer operatorType,
                          String action, Integer oldStatus, Integer newStatus) {
        OrderLog log = new OrderLog();
        log.setOrderId(orderId);
        log.setOperatorId(operatorId);
        log.setOperatorType(operatorType);
        log.setAction(action);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        orderLogMapper.insert(log);
    }
}
