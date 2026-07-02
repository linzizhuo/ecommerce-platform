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
 * 积分服务 — 下单赠送积分、积分兑换优惠券、积分流水
 */
@Service
public class PointService {

    @Resource private UserPointMapper userPointMapper;
    @Resource private PointLogMapper pointLogMapper;
    @Resource private MemberLevelMapper memberLevelMapper;
    @Resource private UserMapper userMapper;

    /** 下单后赠送积分（异步调用） */
    @Transactional
    public void addOrderPoint(Long userId, Integer payAmount) {
        int point = payAmount / 100; // 每消费100分得1积分
        if (point <= 0) return;

        // 获取或创建积分账户
        UserPoint up = userPointMapper.selectById(userId);
        if (up == null) {
            up = new UserPoint();
            up.setUserId(userId);
            up.setTotalPoint(0);
            up.setAvailablePoint(0);
            userPointMapper.insert(up);
        }
        up.setTotalPoint(up.getTotalPoint() + point);
        up.setAvailablePoint(up.getAvailablePoint() + point);
        userPointMapper.updateById(up);

        // 记录流水
        PointLog log = new PointLog();
        log.setUserId(userId);
        log.setPoint(point);
        log.setType(1);
        log.setDescription("下单赠送积分, payAmount=" + payAmount);
        pointLogMapper.insert(log);

        // 检查等级升级
        checkLevelUpgrade(userId, up.getTotalPoint());
    }

    /** 积分兑换优惠券 */
    @Transactional
    public void exchangeCoupon(Long userId, Integer pointCost) {
        UserPoint up = userPointMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserPoint>()
                        .eq(UserPoint::getUserId, userId));
        if (up == null || up.getAvailablePoint() < pointCost)
            throw new BusinessException("积分不足");

        up.setAvailablePoint(up.getAvailablePoint() - pointCost);
        userPointMapper.updateById(up);

        PointLog log = new PointLog();
        log.setUserId(userId);
        log.setPoint(-pointCost);
        log.setType(3);
        log.setDescription("积分兑换优惠券");
        pointLogMapper.insert(log);
    }

    /** 积分流水 */
    public List<PointLog> pointLogs(Long userId) {
        return pointLogMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PointLog>()
                        .eq(PointLog::getUserId, userId)
                        .orderByDesc(PointLog::getCreateTime));
    }

    /** 用户积分 */
    public UserPoint getUserPoint(Long userId) {
        return userPointMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserPoint>()
                        .eq(UserPoint::getUserId, userId));
    }

    /** 会员等级升级 */
    private void checkLevelUpgrade(Long userId, int totalPoint) {
        // 累计消费满50000分→银卡, 200000→金卡, 500000→钻石
        List<MemberLevel> levels = memberLevelMapper.selectList(null);
        User user = userMapper.selectById(userId);
        // 升级逻辑根据累计积分与等级阈值比较
    }

    /** 所有会员等级 */
    public List<MemberLevel> allLevels() {
        return memberLevelMapper.selectList(null);
    }
}
