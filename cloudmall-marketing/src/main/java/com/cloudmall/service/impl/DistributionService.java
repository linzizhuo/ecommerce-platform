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
 * 分销服务 — 分享返佣、佣金结算、提现
 *
 * 分销等级: 1级5% / 2级8% / 3级10%
 * 佣金 = 订单实付金额 * 分销比例
 */
@Service
public class DistributionService {

    @Resource private DistributionMapper distributionMapper;
    @Resource private OrderMapper orderMapper;
    @Resource private UserMapper userMapper;

    /** 用户注册为分销员 */
    public Distribution register(Long userId, Long parentId) {
        Distribution exist = distributionMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Distribution>()
                        .eq(Distribution::getUserId, userId));
        if (exist != null) throw new BusinessException("已是分销员");

        Distribution d = new Distribution();
        d.setUserId(userId);
        d.setParentId(parentId);
        d.setTotalCommission(0);
        d.setAvailableCommission(0);
        d.setLevel(1);
        d.setStatus(1);
        distributionMapper.insert(d);
        return d;
    }

    /** 确认收货后结算佣金 */
    @Transactional
    public void settleCommission(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 3) return;

        // 查找推荐人（从下单时记录的referrerId）
        // 简化处理: 查分销表的parentId链路
        Distribution dist = distributionMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Distribution>()
                        .eq(Distribution::getUserId, order.getUserId()));
        if (dist == null || dist.getParentId() == null) return;

        Distribution parent = distributionMapper.selectById(dist.getParentId());
        if (parent == null) return;

        // 按等级比例计算佣金
        double rate = switch (parent.getLevel()) {
            case 3 -> 0.10;
            case 2 -> 0.08;
            default -> 0.05;
        };
        int commission = (int) (order.getPayAmount() * rate);

        parent.setTotalCommission(parent.getTotalCommission() + commission);
        parent.setAvailableCommission(parent.getAvailableCommission() + commission);
        distributionMapper.updateById(parent);
    }

    /** 提现申请（模拟） */
    @Transactional
    public void withdraw(Long userId, int amount) {
        Distribution d = distributionMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Distribution>()
                        .eq(Distribution::getUserId, userId));
        if (d == null) throw new BusinessException("非分销员");
        if (d.getAvailableCommission() < amount) throw new BusinessException("可提现佣金不足");

        d.setAvailableCommission(d.getAvailableCommission() - amount);
        distributionMapper.updateById(d);
        // TODO: 实际打款对接微信/支付宝企业付款
    }

    /** 分销员信息 */
    public Distribution getInfo(Long userId) {
        return distributionMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Distribution>()
                        .eq(Distribution::getUserId, userId));
    }
}
