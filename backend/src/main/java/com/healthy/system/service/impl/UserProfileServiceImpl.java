package com.healthy.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthy.system.entity.UserProfile;
import com.healthy.system.mapper.UserProfileMapper;
import com.healthy.system.service.UserProfileService;
import org.springframework.stereotype.Service;

@Service
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile> implements UserProfileService {
}

