package com.healthy.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.healthy.system.auth.UserContext;
import com.healthy.system.common.ApiResponse;
import com.healthy.system.entity.TrainAction;
import com.healthy.system.service.TrainActionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/train")
@Validated
public class AdminTrainController {

    private final TrainActionService trainActionService;

    public AdminTrainController(TrainActionService trainActionService) {
        this.trainActionService = trainActionService;
    }

    private boolean isAdmin() {
        String role = UserContext.getRole();
        return "ADMIN".equalsIgnoreCase(role);
    }

    private <T> ApiResponse<T> forbidden() {
        return ApiResponse.error(403, "无权限，仅管理员可访问");
    }

    /**
     * 查询训练动作列表（可选按肌群过滤）
     */
    @GetMapping("/actions")
    public ApiResponse<List<TrainAction>> list(@RequestParam(required = false) String muscleGroup) {
        if (!isAdmin()) {
            return forbidden();
        }
        LambdaQueryWrapper<TrainAction> wrapper = new LambdaQueryWrapper<>();
        if (muscleGroup != null && !muscleGroup.isEmpty()) {
            wrapper.eq(TrainAction::getMuscleGroup, muscleGroup);
        }
        wrapper.orderByDesc(TrainAction::getId);
        List<TrainAction> list = trainActionService.list(wrapper);
        return ApiResponse.success(list);
    }

    /**
     * 新增训练动作
     */
    @PostMapping("/actions")
    public ApiResponse<TrainAction> create(@Valid @RequestBody TrainAction action) {
        if (!isAdmin()) {
            return forbidden();
        }
        action.setId(null);
        trainActionService.save(action);
        return ApiResponse.success(action);
    }

    /**
     * 更新训练动作
     */
    @PutMapping("/actions/{id}")
    public ApiResponse<TrainAction> update(@PathVariable Long id,
                                           @Valid @RequestBody TrainAction action) {
        if (!isAdmin()) {
            return forbidden();
        }
        action.setId(id);
        trainActionService.updateById(action);
        TrainAction db = trainActionService.getById(id);
        return ApiResponse.success(db);
    }

    /**
     * 删除训练动作
     */
    @DeleteMapping("/actions/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (!isAdmin()) {
            return forbidden();
        }
        trainActionService.removeById(id);
        return ApiResponse.success();
    }
}

