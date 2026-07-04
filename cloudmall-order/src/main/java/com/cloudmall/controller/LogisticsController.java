package com.cloudmall.controller;

import com.cloudmall.common.result.R;
import com.cloudmall.service.impl.LogisticsService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logistics")
public class LogisticsController {
    @Resource private LogisticsService logisticsService;

    /** 查询物流轨迹 */
    @GetMapping("/{orderId}")
    public R query(@PathVariable Long orderId) {
        return R.ok(logisticsService.query(orderId));
    }

    /** 商家发货 */
    @PostMapping("/ship")
    public R ship(@RequestParam Long orderId, @RequestParam String company,
                  @RequestParam String trackingNo, HttpServletRequest req) {
        Long merchantId = (Long) req.getAttribute("userId");
        logisticsService.ship(orderId, merchantId, company, trackingNo);
        return R.ok();
    }

    /** 确认收货 */
    @PostMapping("/sign/{orderId}")
    public R sign(@PathVariable Long orderId, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        logisticsService.sign(orderId, userId);
        return R.ok();
    }
}
