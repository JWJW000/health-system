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
     * 返回未来一段时间训练/休息日安排
     */
    @GetMapping("/plan")
    public ApiResponse<Map<String, Object>> trainPlan(@RequestParam(defaultValue = "30") int days) {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.error(401, "未登录");
        }
        List<LocalDate> trainDays = trainActionService.generateTrainDays(accountId, days);
        Map<String, Object> result = new HashMap<>();
        result.put("trainDays", trainDays);
        result.put("days", days);
        return ApiResponse.success(result);
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

