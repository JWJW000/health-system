package com.healthy.system.controller;

import com.healthy.system.common.ApiResponse;
import com.healthy.system.dto.LoginRequest;
import com.healthy.system.dto.LoginResponse;
import com.healthy.system.dto.RegisterRequest;
import com.healthy.system.entity.SysUser;
import com.healthy.system.service.AuthService;
import com.healthy.system.service.SysUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthService authService;
    private final SysUserService sysUserService;

    public AuthController(AuthService authService, SysUserService sysUserService) {
        this.authService = authService;
        this.sysUserService = sysUserService;
    }

    @PostMapping("/register")
    public ApiResponse<String> register(@Valid @RequestBody RegisterRequest request) {
        SysUser user = authService.register(request.getUsername(), request.getPassword());
        return ApiResponse.success("注册成功");
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword());
        // 查询用户信息，带出角色
        SysUser user = sysUserService.findByUsername(request.getUsername());

        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUsername(request.getUsername());
        resp.setRole(user != null ? user.getRole() : "USER");
        return ApiResponse.success(resp);
    }
}

