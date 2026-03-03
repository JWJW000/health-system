package com.healthy.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.healthy.system.entity.SysUser;

public interface SysUserService extends IService<SysUser> {

    SysUser findByUsername(String username);
}

