package com.healthy.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.healthy.system.entity.TrainAction;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

    /**
     * 构建从指定日期开始的一周训练计划：返回每天对应的主要肌群关键字或 REST
     */
    Map<LocalDate, String> buildWeekPlan(Long accountId, LocalDate startDate, int days);

    /**
     * 根据某天计划生成推荐动作列表；若指定 group 则按该肌群返回（用户自选今日训练部位）
     */
    List<TrainAction> buildDayActions(Long accountId, LocalDate date, String group);

    /**
     * 根据用户健身目标返回训练/休息模式文案，如「练三休一」「练四休一」「练五休一」
     */
    String getTrainRestPattern(Long accountId);
}

