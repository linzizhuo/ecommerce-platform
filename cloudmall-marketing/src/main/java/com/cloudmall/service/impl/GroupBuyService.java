package com.cloudmall.service.impl;

import com.cloudmall.common.exception.BusinessException;
import com.cloudmall.entity.*;
import com.cloudmall.mapper.*;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 拼团服务 — 团长开团→团员参团→到期成团/失败
 */
@Service
public class GroupBuyService {

    @Resource private GroupBuyMapper groupBuyMapper;
    @Resource private GroupBuyMemberMapper memberMapper;
    @Resource private ActivityMapper activityMapper;
    @Resource private ActivityProductMapper activityProductMapper;
    @Resource private OrderMapper orderMapper;
    @Resource private SkuMapper skuMapper;
    @Resource private RedissonClient redissonClient;

    /** 团长开团 */
    @Transactional
    public GroupBuy createGroup(Long userId, Long activityId) {
        Activity act = activityMapper.selectById(activityId);
        if (act == null || act.getStatus() != 2) throw new BusinessException("活动不可用");
        if (act.getType() != 2) throw new BusinessException("非拼团活动");

        // 查找活动商品
        List<ActivityProduct> aps = activityProductMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ActivityProduct>()
                        .eq(ActivityProduct::getActivityId, activityId));
        if (aps.isEmpty()) throw new BusinessException("活动商品异常");
        ActivityProduct ap = aps.get(0);

        GroupBuy gb = new GroupBuy();
        gb.setActivityId(activityId);
        gb.setSkuId(ap.getSkuId());
        gb.setLeaderId(userId);
        gb.setRequiredCount(2);
        gb.setCurrentCount(1);
        gb.setGroupPrice(ap.getActivityPrice());
        gb.setStatus(0);
        gb.setExpireTime(LocalDateTime.now().plusHours(24)); // 24小时过期
        groupBuyMapper.insert(gb);

        // 团长加入
        GroupBuyMember member = new GroupBuyMember();
        member.setGroupId(gb.getId());
        member.setUserId(userId);
        member.setIsLeader(1);
        memberMapper.insert(member);

        return gb;
    }

    /** 团员参团 */
    @Transactional
    public Map<String, Object> joinGroup(Long userId, Long groupId) {
        RLock lock = redissonClient.getLock("group:join:" + groupId);
        try {
            if (!lock.tryLock(2, 5, TimeUnit.SECONDS))
                throw new BusinessException("参团繁忙");

            GroupBuy gb = groupBuyMapper.selectById(groupId);
            if (gb == null || gb.getStatus() != 0) throw new BusinessException("拼团已结束");
            if (LocalDateTime.now().isAfter(gb.getExpireTime())) {
                gb.setStatus(2); // 过期失败
                groupBuyMapper.updateById(gb);
                throw new BusinessException("拼团已过期");
            }

            gb.setCurrentCount(gb.getCurrentCount() + 1);
            groupBuyMapper.updateById(gb);

            GroupBuyMember member = new GroupBuyMember();
            member.setGroupId(groupId);
            member.setUserId(userId);
            member.setIsLeader(0);
            memberMapper.insert(member);

            // 检查是否成团
            if (gb.getCurrentCount() >= gb.getRequiredCount()) {
                gb.setStatus(1); // 已成团
                groupBuyMapper.updateById(gb);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("groupId", groupId);
            result.put("message", gb.getStatus() == 1 ? "拼团成功" : "参团成功，等待成团");
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统繁忙");
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    /** 拼团详情 */
    public Map<String, Object> detail(Long groupId) {
        GroupBuy gb = groupBuyMapper.selectById(groupId);
        if (gb == null) return null;
        List<GroupBuyMember> members = memberMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GroupBuyMember>()
                        .eq(GroupBuyMember::getGroupId, groupId));
        Map<String, Object> result = new HashMap<>();
        result.put("group", gb);
        result.put("members", members);
        return result;
    }
}
