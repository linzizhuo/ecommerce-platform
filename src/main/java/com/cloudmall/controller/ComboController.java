package com.cloudmall.controller;

import com.cloudmall.common.result.R;
import com.cloudmall.service.impl.ComboService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/combo")
public class ComboController {
    @Resource private ComboService comboService;

    @GetMapping("/list")
    public R list(@RequestParam(required = false) Long merchantId) {
        return R.ok(comboService.list(merchantId));
    }

    @GetMapping("/{id}")
    public R detail(@PathVariable Long id) {
        return R.ok(comboService.detail(id));
    }
}
