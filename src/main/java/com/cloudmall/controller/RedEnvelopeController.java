package com.cloudmall.controller;

import com.cloudmall.common.result.R;
import com.cloudmall.entity.RedEnvelope;
import com.cloudmall.service.impl.RedEnvelopeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/red-envelope")
public class RedEnvelopeController {

    @Resource
    private RedEnvelopeService redEnvelopeService;

    /** 发送红包 */
    @PostMapping("/send")
    public R<RedEnvelope> send(@RequestAttribute("userId") Long userId,
                                @RequestBody Map<String, Object> body) {
        Integer amount = (Integer) body.get("amount");
        Integer type = body.get("type") != null ? (Integer) body.get("type") : 1;
        String message = (String) body.get("message");
        Long orderId = body.get("orderId") != null ?
                Long.valueOf(body.get("orderId").toString()) : null;
        if (amount == null || amount <= 0) return R.fail("金额不能为空");
        RedEnvelope re = redEnvelopeService.send(userId, amount, type, message, orderId);
        return R.ok(re);
    }

    /** 领取红包 */
    @PostMapping("/receive/{id}")
    public R<RedEnvelope> receive(@RequestAttribute("userId") Long userId,
                                   @PathVariable Long id) {
        try {
            RedEnvelope re = redEnvelopeService.receive(id, userId);
            return R.ok(re);
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    /** 我发送的红包 */
    @GetMapping("/sent")
    public R<List<RedEnvelope>> sent(@RequestAttribute("userId") Long userId) {
        return R.ok(redEnvelopeService.sentList(userId));
    }

    /** 我收到的红包 */
    @GetMapping("/received")
    public R<List<RedEnvelope>> received(@RequestAttribute("userId") Long userId) {
        return R.ok(redEnvelopeService.receivedList(userId));
    }
}
