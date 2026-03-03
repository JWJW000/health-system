package com.healthy.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.healthy.system.auth.UserContext;
import com.healthy.system.common.ApiResponse;
import com.healthy.system.entity.SysUser;
import com.healthy.system.service.SysUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/user")
@Validated
public class AdminUserController {

    private final SysUserService sysUserService;

    public AdminUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    private boolean isAdmin() {
        String role = UserContext.getRole();
        return "ADMIN".equalsIgnoreCase(role);
    }

    private <T> ApiResponse<T> forbidden() {
        return ApiResponse.error(403, "无权限，仅管理员可访问");
    }

    /**
     * 用户列表（可按用户名模糊查询）
     */
    @GetMapping("/list")
    public ApiResponse<List<SysUser>> list(@RequestParam(required = false) String keyword) {
        if (!isAdmin()) {
            return forbidden();
        }
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SysUser::getUsername, keyword);
        }
        wrapper.orderByDesc(SysUser::getId);
        List<SysUser> list = sysUserService.list(wrapper);
        return ApiResponse.success(list);
    }
}

