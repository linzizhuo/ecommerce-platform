package com.cloudmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.common.exception.BusinessException;
import com.cloudmall.entity.Address;
import com.cloudmall.mapper.AddressMapper;
import com.cloudmall.service.AddressService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Resource
    private AddressMapper addressMapper;

    @Override
    public List<Address> listByUserId(Long userId) {
        return addressMapper.selectList(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .orderByDesc(Address::getIsDefault)
                        .orderByDesc(Address::getCreateTime)
        );
    }

    @Override
    public Address getById(Long id) {
        return addressMapper.selectById(id);
    }

    @Override
    public void save(Address address) {
        // 最多20个地址
        long count = addressMapper.selectCount(
                new LambdaQueryWrapper<Address>().eq(Address::getUserId, address.getUserId())
        );
        if (count >= 20) {
            throw new BusinessException("收货地址最多20个");
        }
        addressMapper.insert(address);
    }

    @Override
    public void update(Address address) {
        addressMapper.updateById(address);
    }

    @Override
    public void delete(Long id, Long userId) {
        Address addr = addressMapper.selectById(id);
        if (addr == null || !addr.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
        addressMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void setDefault(Long id, Long userId) {
        // 先取消其他默认
        List<Address> list = listByUserId(userId);
        for (Address a : list) {
            if (a.getIsDefault() == 1) {
                a.setIsDefault(0);
                addressMapper.updateById(a);
            }
        }
        // 设置当前为默认
        Address addr = addressMapper.selectById(id);
        if (addr == null || !addr.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
        addr.setIsDefault(1);
        addressMapper.updateById(addr);
    }
}
