package com.cloudmall.controller;

import com.cloudmall.common.result.R;
import com.cloudmall.entity.Order;
import com.cloudmall.entity.Presale;
import com.cloudmall.service.impl.PresaleService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/presale")
public class PresaleController {
    @Resource private PresaleService presaleService;

    /** 预售活动列表 */
    @GetMapping("/list")
    public R list() {
        List<Presale> list = presaleService.activeList();
        return R.ok(list);
    }

    /** 支付定金 */
    @PostMapping("/deposit")
    public R deposit(@RequestParam Long presaleId, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        Order order = presaleService.payDeposit(userId, presaleId);
        return R.ok(order);
    }

    /** 支付尾款 */
    @PostMapping("/final")
    public R payFinal(@RequestParam Long orderId, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        Order order = presaleService.payFinal(userId, orderId);
        return R.ok(order);
    }
}
