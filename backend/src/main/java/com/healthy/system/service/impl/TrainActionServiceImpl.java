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
import java.util.stream.Collectors;

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
                result.put("types", List.of("慢跑", "跳绳", "游泳", "快走", "椭圆机"));
                result.put("durationMinutes", "每次 30-45 分钟");
                result.put("frequency", "每周 2-3 次");
                result.put("heartRate", "建议维持在最大心率的 60%-75%");
                result.put("scheduleTip", "建议在无氧训练日之后进行有氧，或单独安排有氧日；有氧与无氧间隔至少 4-6 小时更佳。");
                break;
            case BULK:
                result.put("types", List.of("快走", "轻度骑行", "慢跑（控制时长）"));
                result.put("durationMinutes", "每次 15-20 分钟");
                result.put("frequency", "每周 2-3 次");
                result.put("heartRate", "建议控制在最大心率的 55%-65%");
                result.put("scheduleTip", "增肌期有氧不宜过长，以免消耗肌肉；建议安排在无氧训练之后或休息日进行，避免与力量训练抢恢复。");
                break;
            case SHAPE:
                result.put("types", List.of("慢跑", "游泳", "快走", "动感单车"));
                result.put("durationMinutes", "每次 25-40 分钟");
                result.put("frequency", "每周 3-4 次");
                result.put("heartRate", "建议维持在最大心率的 60%-70%");
                result.put("scheduleTip", "可将有氧与无氧交替安排，如：无氧日次日有氧，或同一天先无氧后有氧（有氧 20-30 分钟）。");
                break;
            case FUNCTIONAL:
            default:
                result.put("types", List.of("HIIT 间歇跑", "划船机", "跳绳"));
                result.put("durationMinutes", "每次 20-30 分钟");
                result.put("frequency", "每周 3 次");
                result.put("heartRate", "建议接近最大心率的 70%-85%，注意自我感受");
                result.put("scheduleTip", "功能性训练日可适当加入短时有氧或 HIIT，注意恢复与无氧日错开强度。");
                break;
        }
        return result;
    }

    @Override
    public Map<LocalDate, String> buildWeekPlan(Long accountId, LocalDate startDate, int days) {
        Map<LocalDate, String> plan = new HashMap<>();
        if (days <= 0) {
            return plan;
        }
        // 先确定哪些是训练日
        List<LocalDate> trainDays = generateTrainDays(accountId, days);
        String[] groups = new String[]{"chest", "back", "leg", "shoulder", "core"};
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            if (!trainDays.contains(date)) {
                plan.put(date, "REST");
            } else {
                int index = date.getDayOfWeek().getValue() % groups.length;
                plan.put(date, groups[index]);
            }
        }
        return plan;
    }

    @Override
    public List<TrainAction> buildDayActions(Long accountId, LocalDate date, String group) {
        String key = group;
        if (key == null || key.isEmpty()) {
            Map<LocalDate, String> plan = buildWeekPlan(accountId, date, 1);
            key = plan.getOrDefault(date, "REST");
        }
        if ("REST".equals(key)) {
            return List.of();
        }
        // 根据用户水平和器械偏好简化过滤：优先推荐低难度且徒手/哑铃动作
        UserProfile profile = userProfileService.getById(accountId);
        int expYears = profile != null && profile.getExperienceYears() != null ? profile.getExperienceYears() : 0;

        List<TrainAction> all = listByMuscleGroup(key);
        if (all.isEmpty()) {
            return all;
        }

        // 对动作按难度排序，并根据经验过滤一遍
        return all.stream()
                .sorted((a, b) -> {
                    int da = a.getDifficulty() != null ? a.getDifficulty() : 2;
                    int db = b.getDifficulty() != null ? b.getDifficulty() : 2;
                    return Integer.compare(da, db);
                })
                .filter(a -> {
                    if (expYears <= 1) {
                        // 新手优先 1~2 级难度，徒手或哑铃
                        int d = a.getDifficulty() != null ? a.getDifficulty() : 2;
                        String eq = a.getEquipment() != null ? a.getEquipment() : "BODYWEIGHT";
                        return d <= 2 && ("BODYWEIGHT".equals(eq) || "DUMBBELL".equals(eq));
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    @Override
    public String getTrainRestPattern(Long accountId) {
        UserProfile profile = userProfileService.getById(accountId);
        int pattern;
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
        return "练" + pattern + "休一";
    }
}

