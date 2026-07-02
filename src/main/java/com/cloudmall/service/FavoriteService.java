package com.cloudmall.service;

import com.cloudmall.entity.UserFavorite;
import java.util.List;

public interface FavoriteService {
    void add(Long userId, Long productId);
    void remove(Long userId, Long productId);
    boolean isFavorited(Long userId, Long productId);
    List<UserFavorite> userFavorites(Long userId);
}
