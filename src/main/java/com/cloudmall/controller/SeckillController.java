package com.cloudmall.controller;

import com.cloudmall.common.annotation.RateLimit;
import com.cloudmall.common.result.R;
import com.cloudmall.entity.SeckillSession;
import com.cloudmall.service.impl.SeckillService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seckill")
public class SeckillController {
    @Resource private SeckillService seckillService;

    /** 秒杀活动列表 */
    @GetMapping("/sessions")
    public R sessions() {
        List<SeckillSession> list = seckillService.activeSessions();
        return R.ok(list);
    }

    /** 执行秒杀 — 限流保护 */
    @RateLimit(qps = 200, timeout = 2)
    @PostMapping("/{sessionId}")
    public R seckill(@PathVariable Long sessionId, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        return R.ok(seckillService.seckill(userId, sessionId));
    }
}
