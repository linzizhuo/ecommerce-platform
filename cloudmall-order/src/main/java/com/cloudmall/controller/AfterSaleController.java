package com.cloudmall.controller;

import com.cloudmall.common.result.R;
import com.cloudmall.entity.AfterSale;
import com.cloudmall.service.impl.AfterSaleService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aftersale")
public class AfterSaleController {
    @Resource private AfterSaleService afterSaleService;

    /** 用户申请售后 */
    @PostMapping
    public R apply(@RequestParam Long orderId, @RequestParam Integer type,
                   @RequestParam String reason, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        AfterSale as = afterSaleService.apply(userId, orderId, type, reason);
        return R.ok(as);
    }

    /** 用户售后列表 */
    @GetMapping("/list")
    public R list(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        List<AfterSale> list = afterSaleService.userList(userId);
        return R.ok(list);
    }

    /** 商家审核售后 */
    @PostMapping("/audit")
    public R audit(@RequestParam Long afterSaleId, @RequestParam Integer result,
                   @RequestParam(required = false) String remark, HttpServletRequest req) {
        // merchantId 从JWT或请求属性中获取
        Long merchantId = (Long) req.getAttribute("userId");
        afterSaleService.audit(merchantId, afterSaleId, result, remark);
        return R.ok();
    }
}
