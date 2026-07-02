package com.cloudmall.controller;

import com.cloudmall.common.result.R;
import com.cloudmall.service.impl.StatisticsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    @Resource private StatisticsService statisticsService;

    /** 今日实时快照 */
    @GetMapping("/today")
    public R today() {
        return R.ok(statisticsService.todaySnapshot());
    }

    /** 每日统计 */
    @GetMapping("/daily")
    public R daily(@RequestParam(required = false) String date) {
        LocalDate d = date != null ? LocalDate.parse(date) : LocalDate.now();
        return R.ok(statisticsService.getDailyStat(d));
    }

    /** 近7天趋势 */
    @GetMapping("/week")
    public R week() {
        return R.ok(statisticsService.weekStats());
    }

    /** 近30天趋势 */
    @GetMapping("/month")
    public R month() {
        return R.ok(statisticsService.monthStats());
    }

    /** 订单状态分布 */
    @GetMapping("/order-status-distribution")
    public R orderStatusDistribution() {
        return R.ok(statisticsService.orderStatusDistribution());
    }
}
