package com.healthy.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.healthy.system.entity.DietTemplate;

import java.util.List;

public interface DietTemplateService extends IService<DietTemplate> {

    /**
     * 根据用户当前健身目标推荐食谱列表
     */
    List<DietTemplate> listByUserGoal(Long profileId);
}

