package com.cloudmall.controller;

import com.cloudmall.common.result.R;
import com.cloudmall.entity.Distribution;
import com.cloudmall.service.impl.DistributionService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/distribution")
public class DistributionController {
    @Resource private DistributionService distributionService;

    /** 注册分销员 */
    @PostMapping("/register")
    public R register(@RequestParam(required = false) Long parentId, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        Distribution d = distributionService.register(userId, parentId);
        return R.ok(d);
    }

    /** 我的分销信息 */
    @GetMapping("/my")
    public R myInfo(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        return R.ok(distributionService.getInfo(userId));
    }

    /** 提现 */
    @PostMapping("/withdraw")
    public R withdraw(@RequestParam int amount, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        distributionService.withdraw(userId, amount);
        return R.ok("提现申请已提交");
    }
}
