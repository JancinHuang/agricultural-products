package com.agricultural.products.controller;

import com.agricultural.products.common.Result;
import com.agricultural.products.common.SecurityUtils;
import com.agricultural.products.entity.User;
import com.agricultural.products.service.UserService;
import com.agricultural.products.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器，处理登录、注册和当前用户信息查询。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User user) {
        User existUser = userService.findByUsername(user.getUsername());
        if (existUser == null) {
            return Result.error("用户不存在");
        }

        if (!passwordEncoder.matches(user.getPassword(), existUser.getPassword())) {
            return Result.error("密码错误");
        }
        if (existUser.getStatus() != 1) {
            return Result.error("账号已被禁用");
        }

        String token = jwtUtils.generateToken(existUser.getId(), existUser.getUsername(), existUser.getRole());
        existUser.setPassword(null);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", existUser);
        return Result.success("登录成功", data);
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        boolean success = userService.register(user);
        if (success) {
            return Result.success("注册成功");
        }
        return Result.error("用户名已存在");
    }

    @GetMapping("/info")
    public Result<User> getUserInfo() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userService.findById(userId);
        user.setPassword(null);
        return Result.success(user);
    }
}
