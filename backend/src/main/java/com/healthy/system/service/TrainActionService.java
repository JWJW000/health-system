package com.healthy.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.healthy.system.entity.TrainAction;

import java.time.LocalDate;
import java.util.List;

public interface TrainActionService extends IService<TrainAction> {

    List<TrainAction> listByMuscleGroup(String muscleGroup);

    /**
     * 根据用户健身目标生成训练/休息日数组（简单按日期标记）
     */
    List<LocalDate> generateTrainDays(Long accountId, int days);

    /**
     * 根据用户健身目标返回有氧训练建议
     */
    Object buildAerobicPlan(Long accountId);
}

