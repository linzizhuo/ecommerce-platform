package com.cloudmall.service;

import com.cloudmall.common.result.R;
import com.cloudmall.entity.User;

public interface UserService {

    /**
     * 注册 — BCrypt加密密码
     */
    R<String> register(String phone, String password, String nickname);

    /**
     * 登录 — 校验密码, 返回JWT
     */
    R<String> login(String phone, String password);

    /**
     * 根据ID查用户
     */
    User getById(Long id);
}
