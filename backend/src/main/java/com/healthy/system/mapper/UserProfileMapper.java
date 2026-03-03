package com.healthy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthy.system.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
}

