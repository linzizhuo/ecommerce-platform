package com.cloudmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.common.exception.BusinessException;
import com.cloudmall.entity.UserFavorite;
import com.cloudmall.entity.Product;
import com.cloudmall.mapper.UserFavoriteMapper;
import com.cloudmall.mapper.ProductMapper;
import com.cloudmall.service.FavoriteService;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Resource
    private UserFavoriteMapper favoriteMapper;
    @Resource
    private ProductMapper productMapper;

    @Override
    public void add(Long userId, Long productId) {
        Product p = productMapper.selectById(productId);
        if (p == null) throw new BusinessException("商品不存在");
        UserFavorite f = new UserFavorite();
        f.setUserId(userId);
        f.setProductId(productId);
        try {
            favoriteMapper.insert(f);
        } catch (DuplicateKeyException e) {
            // 已收藏，忽略
        }
    }

    @Override
    public void remove(Long userId, Long productId) {
        favoriteMapper.delete(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getProductId, productId));
    }

    @Override
    public boolean isFavorited(Long userId, Long productId) {
        return favoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getProductId, productId)) > 0;
    }

    @Override
    public List<UserFavorite> userFavorites(Long userId) {
        return favoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .orderByDesc(UserFavorite::getCreateTime));
    }
}
