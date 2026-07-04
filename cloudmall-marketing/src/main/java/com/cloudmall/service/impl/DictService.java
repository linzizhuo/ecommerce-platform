package com.cloudmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.entity.DictItem;
import com.cloudmall.entity.DictType;
import com.cloudmall.mapper.DictItemMapper;
import com.cloudmall.mapper.DictTypeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DictService {

    @Resource
    private DictTypeMapper dictTypeMapper;
    @Resource
    private DictItemMapper dictItemMapper;

    // ===== 字典类型 =====

    public List<DictType> listTypes() {
        return dictTypeMapper.selectList(
            new LambdaQueryWrapper<DictType>().orderByAsc(DictType::getDictCode));
    }

    public DictType saveType(DictType dictType) {
        if (dictType.getId() == null) {
            dictType.setStatus(dictType.getStatus() != null ? dictType.getStatus() : 1);
            dictTypeMapper.insert(dictType);
        } else {
            dictTypeMapper.updateById(dictType);
        }
        return dictType;
    }

    public void deleteType(Long id) {
        // 同时删除关联的字典项
        dictItemMapper.delete(new LambdaQueryWrapper<DictItem>().eq(DictItem::getDictTypeId, id));
        dictTypeMapper.deleteById(id);
    }

    // ===== 字典项 =====

    public List<DictItem> listItems(Long dictTypeId) {
        return dictItemMapper.selectList(
            new LambdaQueryWrapper<DictItem>()
                .eq(DictItem::getDictTypeId, dictTypeId)
                .orderByAsc(DictItem::getSort));
    }

    public List<DictItem> listItemsByCode(String typeCode) {
        DictType dt = dictTypeMapper.selectOne(
            new LambdaQueryWrapper<DictType>().eq(DictType::getDictCode, typeCode));
        if (dt == null) return List.of();
        return listItems(dt.getId());
    }

    public DictItem saveItem(DictItem item) {
        if (item.getId() == null) {
            item.setStatus(item.getStatus() != null ? item.getStatus() : 1);
            item.setSort(item.getSort() != null ? item.getSort() : 0);
            dictItemMapper.insert(item);
        } else {
            dictItemMapper.updateById(item);
        }
        return item;
    }

    public void deleteItem(Long id) {
        dictItemMapper.deleteById(id);
    }
}
