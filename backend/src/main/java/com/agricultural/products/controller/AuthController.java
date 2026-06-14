package com.agricultural.products.controller;

import com.agricultural.products.common.Result;
import com.agricultural.products.common.SecurityUtils;
import com.agricultural.products.dto.LoginRequest;
import com.agricultural.products.dto.RegisterRequest;
import com.agricultural.products.entity.User;
import com.agricultural.products.service.UserService;
import com.agricultural.products.utils.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, JwtUtils jwtUtils, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        User existUser = userService.findByUsername(request.getUsername());
        if (existUser == null) {
            return Result.error(404, "用户不存在");
        }
        if (!passwordEncoder.matches(request.getPassword(), existUser.getPassword())) {
            return Result.error(400, "密码错误");
        }
        if (existUser.getStatus() == null || existUser.getStatus() != 1) {
            return Result.error(403, "账号已被禁用");
        }

        String token = jwtUtils.generateToken(existUser.getId(), existUser.getUsername(), existUser.getRole());
        existUser.setPassword(null);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", existUser);
        return Result.success("登录成功", data);
    }

    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());

        boolean success = userService.register(user);
        return success ? Result.success("注册成功") : Result.error(409, "用户名已存在");
    }

    @GetMapping("/info")
    public Result<User> getUserInfo() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userService.findById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }
}
