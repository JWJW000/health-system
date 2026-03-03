package com.healthy.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthy.system.entity.DietTemplate;
import com.healthy.system.entity.UserProfile;
import com.healthy.system.mapper.DietTemplateMapper;
import com.healthy.system.service.DietTemplateService;
import com.healthy.system.service.UserProfileService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class DietTemplateServiceImpl extends ServiceImpl<DietTemplateMapper, DietTemplate>
        implements DietTemplateService {

    private final UserProfileService userProfileService;

    public DietTemplateServiceImpl(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Override
    public List<DietTemplate> listByUserGoal(Long profileId) {
        UserProfile profile = userProfileService.getById(profileId);
        if (profile == null || profile.getFitnessGoal() == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<DietTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DietTemplate::getFitnessType, profile.getFitnessGoal());
        return this.list(wrapper);
    }
}

