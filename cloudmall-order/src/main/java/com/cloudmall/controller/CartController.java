package com.cloudmall.controller;

import com.cloudmall.common.result.R;
import com.cloudmall.service.impl.CartServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Resource
    private CartServiceImpl cartService;

    @GetMapping
    public R<List<Map<String, Object>>> list(@RequestAttribute("userId") Long userId) {
        return R.ok(cartService.list(userId));
    }

    @PostMapping
    public R<Void> add(@RequestAttribute("userId") Long userId,
                       @RequestBody Map<String, Object> body) {
        cartService.addItem(userId,
                Long.valueOf(body.get("skuId").toString()),
                (String) body.get("productName"),
                (String) body.get("specInfo"),
                (Integer) body.get("price"),
                (Integer) body.get("quantity"),
                (String) body.get("image"));
        return R.ok();
    }

    @PutMapping("/{skuId}")
    public R<Void> updateQuantity(@RequestAttribute("userId") Long userId,
                                   @PathVariable Long skuId,
                                   @RequestBody Map<String, Object> body) {
        cartService.updateQuantity(userId, skuId, (Integer) body.get("quantity"));
        return R.ok();
    }

    @DeleteMapping("/{skuId}")
    public R<Void> remove(@RequestAttribute("userId") Long userId,
                          @PathVariable Long skuId) {
        cartService.removeItem(userId, skuId);
        return R.ok();
    }

    @PutMapping("/{skuId}/check")
    public R<Void> check(@RequestAttribute("userId") Long userId,
                         @PathVariable Long skuId,
                         @RequestBody Map<String, Object> body) {
        cartService.checkItem(userId, skuId, (Boolean) body.get("checked"));
        return R.ok();
    }

    @DeleteMapping("/clear")
    public R<Void> clear(@RequestAttribute("userId") Long userId) {
        cartService.clear(userId);
        return R.ok();
    }
}
