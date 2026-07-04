package com.cloudmall.controller;

import com.cloudmall.common.result.R;
import com.cloudmall.entity.Address;
import com.cloudmall.service.AddressService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址管理 — MVC三层架构第二个示例
 */
@RestController
@RequestMapping("/api/address")
public class AddressController {

    @Resource
    private AddressService addressService;

    /**
     * 地址列表
     */
    @GetMapping
    public R<List<Address>> list(@RequestAttribute("userId") Long userId) {
        return R.ok(addressService.listByUserId(userId));
    }

    /**
     * 新增地址
     */
    @PostMapping
    public R<Void> save(@RequestAttribute("userId") Long userId,
                        @RequestBody Address address) {
        address.setUserId(userId);
        addressService.save(address);
        return R.ok();
    }

    /**
     * 修改地址
     */
    @PutMapping("/{id}")
    public R<Void> update(@RequestAttribute("userId") Long userId,
                          @PathVariable Long id,
                          @RequestBody Address address) {
        address.setId(id);
        address.setUserId(userId);
        addressService.update(address);
        return R.ok();
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@RequestAttribute("userId") Long userId,
                          @PathVariable Long id) {
        addressService.delete(id, userId);
        return R.ok();
    }

    /**
     * 设为默认
     */
    @PutMapping("/{id}/default")
    public R<Void> setDefault(@RequestAttribute("userId") Long userId,
                              @PathVariable Long id) {
        addressService.setDefault(id, userId);
        return R.ok();
    }
}
