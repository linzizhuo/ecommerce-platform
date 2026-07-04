package com.cloudmall.controller;

import com.cloudmall.common.result.R;
import com.cloudmall.service.impl.OrderServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付回调控制器 — 模拟第三方支付网关回调
 */
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Resource
    private OrderServiceImpl orderService;

    /**
     * 支付回调（模拟支付宝/微信支付异步通知）
     *
     * 幂等设计：重复回调不会重复处理（检查订单状态）
     */
    @PostMapping("/callback")
    public R<String> callback(@RequestBody Map<String, Object> body) {
        String orderNo = (String) body.get("orderNo");
        String payNo = (String) body.get("payNo");
        Integer amount = body.get("amount") != null ?
                Integer.valueOf(body.get("amount").toString()) : null;
        Integer payMethod = body.get("payMethod") != null ?
                Integer.valueOf(body.get("payMethod").toString()) : 1;

        if (orderNo == null || payNo == null) {
            return R.fail("参数不完整");
        }

        try {
            orderService.handlePaymentCallback(orderNo, payNo, amount, payMethod, 0L);
            return R.ok("支付成功");
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }
}
