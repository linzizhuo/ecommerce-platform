package com.cloudmall.controller;

import com.cloudmall.common.result.R;
import com.cloudmall.entity.User;
import com.cloudmall.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器 — MVC架构:
 * - 页面路由(Thymeleaf) + API接口(JSON) 共存
 * - Controller调Service, 不直接调Mapper
 * - 密码BCrypt加密 + JWT认证 (vs MV的明文+Session)
 */
@Controller
public class UserController {

    @Resource
    private UserService userService;

    // ==================== 页面 ====================

    @GetMapping("/user/login")
    public String loginPage() {
        return "user/login";
    }

    @GetMapping("/user/register")
    public String registerPage() {
        return "user/register";
    }

    // ==================== API（返回JSON） ====================

    /**
     * 注册API — MVC三层示范
     */
    @PostMapping("/api/user/register")
    @ResponseBody
    public R<String> apiRegister(@RequestParam String phone,
                                  @RequestParam String password,
                                  @RequestParam String nickname) {
        return userService.register(phone, password, nickname);
    }

    /**
     * 登录API — MVC三层示范
     */
    @PostMapping("/api/user/login")
    @ResponseBody
    public R<String> apiLogin(@RequestParam String phone,
                               @RequestParam String password) {
        return userService.login(phone, password);
    }

    /**
     * 获取当前用户信息 — 需要JWT认证
     */
    @GetMapping("/api/user/info")
    @ResponseBody
    public R<User> info(@RequestAttribute("userId") Long userId) {
        User user = userService.getById(userId);
        user.setPassword(null); // 不返回密码
        return R.ok(user);
    }

    // ==================== 页面表单（兼容旧方式） ====================

    /**
     * 页面注册 — 表单提交
     */
    @PostMapping("/user/register")
    public String doRegister(@RequestParam String phone,
                             @RequestParam String password,
                             @RequestParam String nickname,
                             HttpSession session, Model model) {
        try {
            R<String> result = userService.register(phone, password, nickname);
            // 注册成功, 存session, 跳首页
            session.setAttribute("token", result.getData());
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "user/register";
        }
    }

    /**
     * 页面登录 — 表单提交
     */
    @PostMapping("/user/login")
    public String doLogin(@RequestParam String phone,
                          @RequestParam String password,
                          HttpSession session, Model model) {
        try {
            R<String> result = userService.login(phone, password);
            session.setAttribute("token", result.getData());
            // 手动解析token把用户信息放入session用于页面展示
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "user/login";
        }
    }

    @GetMapping("/user/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
