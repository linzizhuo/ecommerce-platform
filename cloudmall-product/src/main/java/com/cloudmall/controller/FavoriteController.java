package com.cloudmall.controller;

import com.cloudmall.common.result.R;
import com.cloudmall.entity.UserFavorite;
import com.cloudmall.service.FavoriteService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {
    @Resource private FavoriteService favoriteService;

    @PostMapping("/{productId}")
    public R add(@PathVariable Long productId, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        favoriteService.add(userId, productId);
        return R.ok();
    }

    @DeleteMapping("/{productId}")
    public R remove(@PathVariable Long productId, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        favoriteService.remove(userId, productId);
        return R.ok();
    }

    @GetMapping("/check/{productId}")
    public R check(@PathVariable Long productId, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        return R.ok(favoriteService.isFavorited(userId, productId));
    }

    @GetMapping("/list")
    public R list(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        List<UserFavorite> list = favoriteService.userFavorites(userId);
        return R.ok(list);
    }
}
