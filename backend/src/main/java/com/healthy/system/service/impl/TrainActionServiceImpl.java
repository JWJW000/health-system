package com.healthy.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthy.system.entity.TrainAction;
import com.healthy.system.entity.UserProfile;
import com.healthy.system.enums.FitnessGoalEnum;
import com.healthy.system.mapper.TrainActionMapper;
import com.healthy.system.service.TrainActionService;
import com.healthy.system.service.UserProfileService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TrainActionServiceImpl extends ServiceImpl<TrainActionMapper, TrainAction>
        implements TrainActionService {

    private final UserProfileService userProfileService;

    public TrainActionServiceImpl(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Override
    public List<TrainAction> listByMuscleGroup(String muscleGroup) {
        LambdaQueryWrapper<TrainAction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainAction::getMuscleGroup, muscleGroup);
        return this.list(wrapper);
    }

    @Override
    public List<LocalDate> generateTrainDays(Long accountId, int days) {
        UserProfile profile = userProfileService.getById(accountId);
        int pattern; // 训练天数：休息1天
        if (profile == null || profile.getFitnessGoal() == null) {
            pattern = 3;
        } else {
            FitnessGoalEnum goal = FitnessGoalEnum.fromCode(profile.getFitnessGoal());
            switch (goal) {
                case BULK:
                    pattern = 4; // 练四休一
                    break;
                case CUT:
                    pattern = 3; // 练三休一
                    break;
                case SHAPE:
                    pattern = 4;
                    break;
                case FUNCTIONAL:
                default:
                    pattern = 5; // 练五休一
                    break;
            }
        }
        List<LocalDate> trainDays = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < days; i++) {
            LocalDate date = today.plusDays(i);
            int cycleIndex = i % (pattern + 1);
            if (cycleIndex < pattern) {
                trainDays.add(date);
            }
        }
        return trainDays;
    }

    @Override
    public Object buildAerobicPlan(Long accountId) {
        UserProfile profile = userProfileService.getById(accountId);
        FitnessGoalEnum goal = profile != null && profile.getFitnessGoal() != null
                ? FitnessGoalEnum.fromCode(profile.getFitnessGoal())
                : FitnessGoalEnum.CUT;

        Map<String, Object> result = new HashMap<>();
        switch (goal) {
            case CUT:
                result.put("types", List.of("慢跑", "跳绳", "椭圆机"));
                result.put("durationMinutes", "每次 30-45 分钟");
                result.put("frequency", "每周 3-5 次");
                result.put("heartRate", "建议维持在最大心率的 60%-75%");
                break;
            case BULK:
                result.put("types", List.of("快走", "轻度骑行"));
                result.put("durationMinutes", "每次 15-20 分钟");
                result.put("frequency", "每周 2-3 次");
                result.put("heartRate", "建议控制在最大心率的 55%-65%");
                break;
            case SHAPE:
                result.put("types", List.of("慢跑", "游泳", "动感单车"));
                result.put("durationMinutes", "每次 25-40 分钟");
                result.put("frequency", "每周 3-4 次");
                result.put("heartRate", "建议维持在最大心率的 60%-70%");
                break;
            case FUNCTIONAL:
            default:
                result.put("types", List.of("HIIT 间歇跑", "划船机"));
                result.put("durationMinutes", "每次 20-30 分钟");
                result.put("frequency", "每周 3 次");
                result.put("heartRate", "建议接近最大心率的 70%-85%，注意自我感受");
                break;
        }
        return result;
    }
}

