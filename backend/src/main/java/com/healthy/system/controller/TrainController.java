package com.healthy.system.controller;

import com.healthy.system.auth.UserContext;
import com.healthy.system.common.ApiResponse;
import com.healthy.system.entity.TrainAction;
import com.healthy.system.service.TrainActionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/train")
public class TrainController {

    private final TrainActionService trainActionService;

    public TrainController(TrainActionService trainActionService) {
        this.trainActionService = trainActionService;
    }

    /**
     * 按肌群查询无氧动作
     */
    @GetMapping("/actions")
    public ApiResponse<List<TrainAction>> listActions(@RequestParam String group) {
        List<TrainAction> list = trainActionService.listByMuscleGroup(group);
        return ApiResponse.success(list);
    }

    /**
     * 今日训练计划（简版）：给出今天是否训练日及推荐肌群（仅用于首页展示）
     */
    @GetMapping("/today-plan")
    public ApiResponse<Map<String, Object>> todayPlan(@RequestParam(defaultValue = "14") int days) {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.error(401, "未登录");
        }
        // 复用现有训练日生成规则，判断今天是否训练日
        List<LocalDate> trainDays = trainActionService.generateTrainDays(accountId, days);
        LocalDate today = LocalDate.now();
        boolean isTrainDay = trainDays.contains(today);

        Map<String, Object> result = new HashMap<>();
        result.put("isTrainDay", isTrainDay);
        if (!isTrainDay) {
            result.put("primaryGroup", null);
            result.put("groups", List.of());
            return ApiResponse.success(result);
        }

        // 简单按照一周循环安排肌群：胸/背/腿/肩/核心
        String[] groups = new String[]{"chest", "back", "leg", "shoulder", "core"};
        int index = today.getDayOfWeek().getValue() % groups.length;
        String primaryGroup = groups[index];

        result.put("primaryGroup", primaryGroup);
        result.put("groups", List.of(primaryGroup));
        return ApiResponse.success(result);
    }

    /**
     * 返回未来一段时间训练/休息日安排，及当前训练模式（练三休一/练四休一/练五休一）
     */
    @GetMapping("/plan")
    public ApiResponse<Map<String, Object>> trainPlan(@RequestParam(defaultValue = "30") int days) {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.error(401, "未登录");
        }
        List<LocalDate> trainDays = trainActionService.generateTrainDays(accountId, days);
        String patternLabel = trainActionService.getTrainRestPattern(accountId);
        Map<String, Object> result = new HashMap<>();
        result.put("trainDays", trainDays);
        result.put("days", days);
        result.put("patternLabel", patternLabel);
        return ApiResponse.success(result);
    }

    /**
     * 周训练计划：从指定日期开始，返回若干天的主要训练类型/肌群
     */
    @GetMapping("/week-plan")
    public ApiResponse<Map<String, Object>> weekPlan(@RequestParam(required = false) String start,
                                                     @RequestParam(defaultValue = "7") int days) {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.error(401, "未登录");
        }
        LocalDate startDate = (start != null && !start.isEmpty())
                ? LocalDate.parse(start)
                : LocalDate.now();
        Map<LocalDate, String> plan = trainActionService.buildWeekPlan(accountId, startDate, days);
        Map<String, Object> result = new HashMap<>();
        result.put("start", startDate);
        result.put("days", days);
        result.put("plan", plan);
        return ApiResponse.success(result);
    }

    /**
     * 某天推荐无氧动作列表；可选 group 指定肌群（用户自选今日训练部位时传参）
     */
    @GetMapping("/day-detail")
    public ApiResponse<List<TrainAction>> dayDetail(@RequestParam String date,
                                                    @RequestParam(required = false) String group) {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.error(401, "未登录");
        }
        LocalDate target = LocalDate.parse(date);
        List<TrainAction> list = trainActionService.buildDayActions(accountId, target, group);
        return ApiResponse.success(list);
    }

    /**
     * 有氧训练推荐参数
     */
    @GetMapping("/aerobic")
    public ApiResponse<Object> aerobic() {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.error(401, "未登录");
        }
        Object plan = trainActionService.buildAerobicPlan(accountId);
        return ApiResponse.success(plan);
    }
}

