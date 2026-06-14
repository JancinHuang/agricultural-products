package com.agricultural.products.controller;

import com.agricultural.products.common.Result;
import com.agricultural.products.dto.LoginRequest;
import com.agricultural.products.entity.User;
import com.agricultural.products.service.UserService;
import com.agricultural.products.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private UserService userService;
    private JwtUtils jwtUtils;
    private PasswordEncoder passwordEncoder;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        jwtUtils = mock(JwtUtils.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authController = new AuthController(userService, jwtUtils, passwordEncoder);
    }

    @Test
    void loginReturnsTokenAndMasksPassword() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("encoded");
        user.setRole(1);
        user.setStatus(1);

        when(userService.findByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("admin", "encoded")).thenReturn(true);
        when(jwtUtils.generateToken(1L, "admin", 1)).thenReturn("token");

        Result<Map<String, Object>> result = authController.login(loginRequest("admin", "admin"));

        assertEquals(200, result.getCode());
        assertEquals("token", result.getData().get("token"));
        User responseUser = (User) result.getData().get("user");
        assertNull(responseUser.getPassword());
    }

    @Test
    void loginReturnsNotFoundWhenUserDoesNotExist() {
        when(userService.findByUsername("missing")).thenReturn(null);

        Result<Map<String, Object>> result = authController.login(loginRequest("missing", "123456"));

        assertEquals(404, result.getCode());
        verifyNoInteractions(passwordEncoder, jwtUtils);
    }

    @Test
    void loginReturnsBadRequestWhenPasswordWrong() {
        User user = new User();
        user.setUsername("user1");
        user.setPassword("encoded");
        user.setStatus(1);

        when(userService.findByUsername("user1")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        Result<Map<String, Object>> result = authController.login(loginRequest("user1", "wrong"));

        assertEquals(400, result.getCode());
        verifyNoInteractions(jwtUtils);
    }

    private LoginRequest loginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }
}
