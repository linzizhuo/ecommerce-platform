package com.cloudmall.controller;

import com.cloudmall.common.result.R;
import com.cloudmall.entity.GroupBuy;
import com.cloudmall.service.impl.GroupBuyService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groupbuy")
public class GroupBuyController {
    @Resource private GroupBuyService groupBuyService;

    /** 开团 */
    @PostMapping("/create")
    public R create(@RequestParam Long activityId, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        GroupBuy gb = groupBuyService.createGroup(userId, activityId);
        return R.ok(gb);
    }

    /** 参团 */
    @PostMapping("/join/{groupId}")
    public R join(@PathVariable Long groupId, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        return R.ok(groupBuyService.joinGroup(userId, groupId));
    }

    /** 拼团详情 */
    @GetMapping("/detail/{groupId}")
    public R detail(@PathVariable Long groupId) {
        return R.ok(groupBuyService.detail(groupId));
    }
}
