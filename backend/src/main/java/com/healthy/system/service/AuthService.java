package com.healthy.system.service;

import com.healthy.system.entity.SysUser;

public interface AuthService {

    SysUser register(String username, String rawPassword);

    String login(String username, String rawPassword);

    void changePassword(Long userId, String oldPassword, String newPassword);
}

