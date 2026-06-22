package com.cloudmall.controller;

import com.cloudmall.entity.User;
import com.cloudmall.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 用户控制器 — MV架构: Controller直接调Mapper
 */
@Controller
public class UserController {

    @Resource
    private UserMapper userMapper;

    // ---------- 页面 ----------

    @GetMapping("/user/login")
    public String loginPage() {
        return "user/login";
    }

    @GetMapping("/user/register")
    public String registerPage() {
        return "user/register";
    }

    // ---------- 接口 ----------

    /**
     * 注册
     */
    @PostMapping("/user/register")
    public String doRegister(User user, Model model) {
        // 检查手机号是否已注册
        User exist = userMapper.selectByPhone(user.getPhone());
        if (exist != null) {
            model.addAttribute("error", "手机号已注册");
            return "user/register";
        }
        // 明文存密码 (MV阶段简化, MVC阶段升级BCrypt)
        userMapper.insert(user);
        return "redirect:/user/login";
    }

    /**
     * 登录
     */
    @PostMapping("/user/login")
    public String doLogin(String phone, String password,
                          HttpSession session, Model model) {
        User user = userMapper.selectByPhone(phone);
        if (user == null || !user.getPassword().equals(password)) {
            model.addAttribute("error", "手机号或密码错误");
            return "user/login";
        }
        // Session 存储登录态 (MV阶段, MVC阶段升级JWT)
        session.setAttribute("user", user);
        return "redirect:/";
    }

    /**
     * 退出
     */
    @GetMapping("/user/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
