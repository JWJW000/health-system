package com.healthy.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.healthy.system.auth.UserContext;
import com.healthy.system.common.ApiResponse;
import com.healthy.system.entity.DietTemplate;
import com.healthy.system.service.DietTemplateService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/diet")
@Validated
public class AdminDietController {

    private final DietTemplateService dietTemplateService;

    public AdminDietController(DietTemplateService dietTemplateService) {
        this.dietTemplateService = dietTemplateService;
    }

    private boolean isAdmin() {
        String role = UserContext.getRole();
        return "ADMIN".equalsIgnoreCase(role);
    }

    private <T> ApiResponse<T> forbidden() {
        return ApiResponse.error(403, "无权限，仅管理员可访问");
    }

    /**
     * 按条件查询食谱模板列表（可选按健身目标过滤）
     */
    @GetMapping("/templates")
    public ApiResponse<List<DietTemplate>> list(
            @RequestParam(required = false) Integer fitnessType) {
        if (!isAdmin()) {
            return forbidden();
        }
        LambdaQueryWrapper<DietTemplate> wrapper = new LambdaQueryWrapper<>();
        if (fitnessType != null) {
            wrapper.eq(DietTemplate::getFitnessType, fitnessType);
        }
        wrapper.orderByDesc(DietTemplate::getId);
        List<DietTemplate> list = dietTemplateService.list(wrapper);
        return ApiResponse.success(list);
    }

    /**
     * 新增食谱模板
     */
    @PostMapping("/templates")
    public ApiResponse<DietTemplate> create(@Valid @RequestBody DietTemplate template) {
        if (!isAdmin()) {
            return forbidden();
        }
        template.setId(null);
        dietTemplateService.save(template);
        return ApiResponse.success(template);
    }

    /**
     * 更新食谱模板
     */
    @PutMapping("/templates/{id}")
    public ApiResponse<DietTemplate> update(@PathVariable Long id,
                                            @Valid @RequestBody DietTemplate template) {
        if (!isAdmin()) {
            return forbidden();
        }
        template.setId(id);
        dietTemplateService.updateById(template);
        DietTemplate db = dietTemplateService.getById(id);
        return ApiResponse.success(db);
    }

    /**
     * 删除食谱模板
     */
    @DeleteMapping("/templates/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (!isAdmin()) {
            return forbidden();
        }
        dietTemplateService.removeById(id);
        return ApiResponse.success();
    }
}

