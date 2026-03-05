package com.healthy.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.healthy.system.auth.UserContext;
import com.healthy.system.common.ApiResponse;
import com.healthy.system.entity.SysUser;
import com.healthy.system.service.SysUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /**
     * 修改用户角色
     */
    @PostMapping("/role")
    public ApiResponse<Void> updateRole(@RequestBody UpdateUserRoleRequest request) {
        if (!isAdmin()) {
            return forbidden();
        }
        if (request == null || request.getUserId() == null || request.getRole() == null) {
            return ApiResponse.error(400, "参数不完整");
        }
        String role = request.getRole().toUpperCase();
        if (!"ADMIN".equals(role) && !"USER".equals(role)) {
            return ApiResponse.error(400, "不支持的角色类型");
        }
        SysUser user = sysUserService.getById(request.getUserId());
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        user.setRole(role);
        sysUserService.updateById(user);
        return ApiResponse.success();
    }

    public static class UpdateUserRoleRequest {
        private Long userId;
        private String role;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}

