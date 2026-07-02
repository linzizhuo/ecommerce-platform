package com.cloudmall.controller;

import com.cloudmall.common.result.R;
import com.cloudmall.entity.MemberLevel;
import com.cloudmall.entity.PointLog;
import com.cloudmall.entity.UserPoint;
import com.cloudmall.service.impl.PointService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/point")
public class PointController {
    @Resource private PointService pointService;

    @GetMapping("/my")
    public R myPoint(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        UserPoint up = pointService.getUserPoint(userId);
        return R.ok(up);
    }

    @GetMapping("/logs")
    public R logs(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        List<PointLog> logs = pointService.pointLogs(userId);
        return R.ok(logs);
    }

    @GetMapping("/levels")
    public R levels() {
        List<MemberLevel> levels = pointService.allLevels();
        return R.ok(levels);
    }

    @PostMapping("/exchange")
    public R exchange(@RequestParam Integer pointCost, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        pointService.exchangeCoupon(userId, pointCost);
        return R.ok("兑换成功");
    }
}
