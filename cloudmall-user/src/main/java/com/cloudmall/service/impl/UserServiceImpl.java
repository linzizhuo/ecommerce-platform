package com.cloudmall.service.impl;

import com.cloudmall.common.exception.BusinessException;
import com.cloudmall.common.result.R;
import com.cloudmall.common.utils.JwtUtil;
import com.cloudmall.entity.User;
import com.cloudmall.mapper.UserMapper;
import com.cloudmall.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

/**
 * MVC架构核心改造: 业务逻辑从Controller下沉到Service
 * - BCrypt加密 → Controller不知道加密细节
 * - JWT签发 → Controller不碰token生成
 * - 业务校验 → 集中在Service, 抛异常由GlobalExceptionHandler处理
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public R<String> register(String phone, String password, String nickname) {
        // 业务校验
        User exist = userMapper.selectByPhone(phone);
        if (exist != null) {
            throw new BusinessException("手机号已注册");
        }
        if (password.length() < 6) {
            throw new BusinessException("密码长度不能少于6位");
        }

        User user = new User();
        user.setPhone(phone);
        // MVC改造: 明文 → BCrypt加密
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setNickname(nickname);
        userMapper.insert(user);

        // 注册后自动登录, 返回JWT
        String token = JwtUtil.generate(user.getId(), user.getPhone());
        return R.ok(token);
    }

    @Override
    public R<String> login(String phone, String password) {
        User user = userMapper.selectByPhone(phone);
        if (user == null) {
            throw new BusinessException("手机号或密码错误");
        }
        // MVC改造: 明文比对 → BCrypt校验
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new BusinessException("手机号或密码错误");
        }

        String token = JwtUtil.generate(user.getId(), user.getPhone());
        return R.ok(token);
    }

    @Override
    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }
}
