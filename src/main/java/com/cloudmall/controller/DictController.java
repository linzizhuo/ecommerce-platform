package com.cloudmall.controller;

import com.cloudmall.common.result.R;
import com.cloudmall.entity.DictItem;
import com.cloudmall.entity.DictType;
import com.cloudmall.service.impl.DictService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dict")
public class DictController {

    @Resource
    private DictService dictService;

    // ===== 字典类型 =====

    @GetMapping("/types")
    public R<List<DictType>> listTypes() {
        return R.ok(dictService.listTypes());
    }

    @PostMapping("/type")
    public R<DictType> saveType(@RequestBody DictType dictType) {
        return R.ok(dictService.saveType(dictType));
    }

    @PutMapping("/type/{id}")
    public R<DictType> updateType(@PathVariable Long id, @RequestBody DictType dictType) {
        dictType.setId(id);
        return R.ok(dictService.saveType(dictType));
    }

    @DeleteMapping("/type/{id}")
    public R<Void> deleteType(@PathVariable Long id) {
        dictService.deleteType(id);
        return R.ok();
    }

    // ===== 字典项 =====

    @GetMapping("/type/{typeCode}/items")
    public R<List<DictItem>> itemsByCode(@PathVariable String typeCode) {
        return R.ok(dictService.listItemsByCode(typeCode));
    }

    @PostMapping("/item")
    public R<DictItem> saveItem(@RequestBody DictItem item) {
        return R.ok(dictService.saveItem(item));
    }

    @PutMapping("/item/{id}")
    public R<DictItem> updateItem(@PathVariable Long id, @RequestBody DictItem item) {
        item.setId(id);
        return R.ok(dictService.saveItem(item));
    }

    @DeleteMapping("/item/{id}")
    public R<Void> deleteItem(@PathVariable Long id) {
        dictService.deleteItem(id);
        return R.ok();
    }
}
