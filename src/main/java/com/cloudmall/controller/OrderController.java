package com.cloudmall.controller;

import com.cloudmall.common.result.R;
import com.cloudmall.entity.Order;
import com.cloudmall.service.impl.OrderServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Resource
    private OrderServiceImpl orderService;

    /** 下单 */
    @PostMapping
    public R<Map<String, Object>> create(@RequestAttribute("userId") Long userId,
                                          @RequestBody Map<String, Object> body) {
        Map<String, Object> result = orderService.create(
                userId,
                body.get("addressId") != null ? Long.valueOf(body.get("addressId").toString()) : null,
                body.get("couponId") != null ? Long.valueOf(body.get("couponId").toString()) : null,
                (String) body.get("addressSnapshot"),
                body.get("payMethod") != null ? (Integer) body.get("payMethod") : 1
        );
        return R.ok(result);
    }

    /** 支付 */
    @PostMapping("/{id}/pay")
    public R<Void> pay(@RequestAttribute("userId") Long userId,
                       @PathVariable Long id,
                       @RequestBody Map<String, Object> body) {
        orderService.pay(id, userId, (Integer) body.getOrDefault("payMethod", 1));
        return R.ok();
    }

    /** 取消订单 */
    @PostMapping("/{id}/cancel")
    public R<Void> cancel(@RequestAttribute("userId") Long userId,
                          @PathVariable Long id) {
        orderService.cancel(id, userId);
        return R.ok();
    }

    /** 商家改单 */
    @PutMapping("/{id}")
    public R<Void> modify(@PathVariable Long id,
                          @RequestBody Map<String, Object> body) {
        orderService.modify(id, body);
        return R.ok();
    }

    /** 商家发货 */
    @PostMapping("/{id}/ship")
    public R<Void> ship(@PathVariable Long id,
                        @RequestBody Map<String, String> body) {
        orderService.ship(id, body.get("company"), body.get("trackingNo"));
        return R.ok();
    }

    /** 确认收货 */
    @PostMapping("/{id}/confirm")
    public R<Void> confirm(@RequestAttribute("userId") Long userId,
                           @PathVariable Long id) {
        orderService.confirm(id, userId);
        return R.ok();
    }

    /** 订单列表 */
    @GetMapping
    public R<List<Order>> list(@RequestAttribute("userId") Long userId,
                                @RequestParam(required = false) Integer status) {
        return R.ok(orderService.userOrders(userId, status));
    }

    /** 订单详情 */
    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        return R.ok(orderService.detail(id));
    }
}
