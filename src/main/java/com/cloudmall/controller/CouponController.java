package com.cloudmall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.common.result.R;
import com.cloudmall.entity.CouponTemplate;
import com.cloudmall.entity.UserCoupon;
import com.cloudmall.mapper.CouponTemplateMapper;
import com.cloudmall.mapper.UserCouponMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/coupon")
public class CouponController {

    @Resource private CouponTemplateMapper templateMapper;
    @Resource private UserCouponMapper userCouponMapper;

    /** 商家优惠券列表 */
    @GetMapping("/merchant")
    public R<List<CouponTemplate>> merchantList() {
        return R.ok(templateMapper.selectList(
            new LambdaQueryWrapper<CouponTemplate>().orderByDesc(CouponTemplate::getCreateTime)));
    }

    /** 新建优惠券 */
    @PostMapping("/merchant")
    public R<Void> create(@RequestBody Map<String, Object> body) {
        CouponTemplate ct = new CouponTemplate();
        ct.setName((String) body.get("name"));
        ct.setType((Integer) body.getOrDefault("type", 1));
        ct.setThreshold((Integer) body.getOrDefault("threshold", 0));
        ct.setValue((Integer) body.get("value"));
        ct.setTotalCount((Integer) body.getOrDefault("totalCount", 100));
        ct.setStartTime(LocalDateTime.now());
        ct.setEndTime(LocalDateTime.now().plusDays(30));
        ct.setCreateTime(LocalDateTime.now());
        templateMapper.insert(ct);
        return R.ok();
    }

    /** 删除优惠券 */
    @DeleteMapping("/merchant/{id}")
    public R<Void> delete(@PathVariable Long id) {
        templateMapper.deleteById(id);
        return R.ok();
    }

    /** 用户可领优惠券列表 */
    @GetMapping("/available")
    public R<List<CouponTemplate>> available(@RequestAttribute("userId") Long userId) {
        List<CouponTemplate> all = templateMapper.selectList(
            new LambdaQueryWrapper<CouponTemplate>().gt(CouponTemplate::getTotalCount, 0));
        List<Long> received = userCouponMapper.selectList(
            new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getUserId, userId))
            .stream().map(UserCoupon::getTemplateId).toList();
        all.removeIf(t -> received.contains(t.getId()));
        return R.ok(all);
    }

    /** 用户优惠券 */
    @GetMapping("/my")
    public R<List<Map<String, Object>>> my(@RequestAttribute("userId") Long userId) {
        List<UserCoupon> list = userCouponMapper.selectList(
            new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getUserId, userId));
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserCoupon uc : list) {
            CouponTemplate ct = templateMapper.selectById(uc.getTemplateId());
            Map<String, Object> m = new HashMap<>();
            m.put("id", uc.getId()); m.put("templateId", uc.getTemplateId());
            m.put("name", ct != null ? ct.getName() : ""); m.put("status", uc.getStatus());
            m.put("threshold", ct != null ? ct.getThreshold() : 0);
            m.put("value", ct != null ? ct.getValue() : 0);
            m.put("expireTime", uc.getExpireTime());
            result.add(m);
        }
        return R.ok(result);
    }

    /** 领取优惠券 */
    @PostMapping("/receive/{templateId}")
    public R<Void> receive(@RequestAttribute("userId") Long userId, @PathVariable Long templateId) {
        long count = userCouponMapper.selectCount(
            new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getTemplateId, templateId));
        if (count > 0) return R.fail("已领取过");
        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId); uc.setTemplateId(templateId);
        uc.setStatus(0); uc.setExpireTime(LocalDateTime.now().plusDays(14));
        uc.setCreateTime(LocalDateTime.now());
        userCouponMapper.insert(uc);
        return R.ok();
    }
}
