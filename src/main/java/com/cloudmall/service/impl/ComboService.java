package com.cloudmall.service.impl;

import com.cloudmall.entity.*;
import com.cloudmall.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 组合套餐服务 — 多个商品打包优惠出售
 */
@Service
public class ComboService {

    @Resource private ComboPackageMapper comboMapper;
    @Resource private ComboItemMapper comboItemMapper;
    @Resource private SkuMapper skuMapper;

    /** 获取所有有效套餐 */
    public List<ComboPackage> list(Long merchantId) {
        if (merchantId != null) {
            return comboMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ComboPackage>()
                            .eq(ComboPackage::getMerchantId, merchantId)
                            .eq(ComboPackage::getStatus, 1));
        }
        return comboMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ComboPackage>()
                        .eq(ComboPackage::getStatus, 1));
    }

    /** 套餐详情（含商品明细） */
    public Map<String, Object> detail(Long comboId) {
        ComboPackage combo = comboMapper.selectById(comboId);
        if (combo == null) return null;
        List<ComboItem> items = comboItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ComboItem>()
                        .eq(ComboItem::getComboId, comboId));
        Map<String, Object> result = new HashMap<>();
        result.put("combo", combo);
        result.put("items", items);
        return result;
    }

    /** 商家创建套餐 */
    public void save(ComboPackage combo, List<ComboItem> items) {
        if (combo.getId() == null) comboMapper.insert(combo);
        else comboMapper.updateById(combo);

        if (items != null && !items.isEmpty()) {
            comboItemMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ComboItem>()
                    .eq(ComboItem::getComboId, combo.getId()));
            for (ComboItem item : items) {
                item.setComboId(combo.getId());
                comboItemMapper.insert(item);
            }
        }
    }
}
