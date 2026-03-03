package com.healthy.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.healthy.system.auth.UserContext;
import com.healthy.system.common.ApiResponse;
import com.healthy.system.dto.DietCollectRequest;
import com.healthy.system.entity.DietCollect;
import com.healthy.system.entity.DietTemplate;
import com.healthy.system.entity.SysUser;
import com.healthy.system.service.DietCollectService;
import com.healthy.system.service.DietTemplateService;
import com.healthy.system.service.SysUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/diet")
@Validated
public class DietController {

    private final DietTemplateService dietTemplateService;
    private final DietCollectService dietCollectService;
    private final SysUserService sysUserService;

    public DietController(DietTemplateService dietTemplateService,
                          DietCollectService dietCollectService,
                          SysUserService sysUserService) {
        this.dietTemplateService = dietTemplateService;
        this.dietCollectService = dietCollectService;
        this.sysUserService = sysUserService;
    }

    /**
     * 基于用户健身目标返回推荐食谱列表
     */
    @GetMapping("/list")
    public ApiResponse<List<DietTemplate>> listByUser() {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.error(401, "未登录");
        }
        SysUser account = sysUserService.getById(accountId);
        if (account == null || account.getProfileId() == null) {
            return ApiResponse.success(List.of());
        }
        List<DietTemplate> list = dietTemplateService.listByUserGoal(account.getProfileId());
        return ApiResponse.success(list);
    }

    /**
     * 单个食谱详情
     */
    @GetMapping("/{id}")
    public ApiResponse<DietTemplate> detail(@PathVariable Long id) {
        DietTemplate template = dietTemplateService.getById(id);
        return ApiResponse.success(template);
    }

    /**
     * 收藏
     */
    @PostMapping("/collect")
    public ApiResponse<Void> collect(@Valid @RequestBody DietCollectRequest request) {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.error(401, "未登录");
        }
        LambdaQueryWrapper<DietCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DietCollect::getUserId, accountId)
                .eq(DietCollect::getDietId, request.getDietId());
        DietCollect exist = dietCollectService.getOne(wrapper, false);
        if (exist == null) {
            DietCollect collect = new DietCollect();
            collect.setUserId(accountId);
            collect.setDietId(request.getDietId());
            dietCollectService.save(collect);
        }
        return ApiResponse.success();
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/collect")
    public ApiResponse<Void> cancelCollect(@Valid @RequestBody DietCollectRequest request) {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.error(401, "未登录");
        }
        LambdaQueryWrapper<DietCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DietCollect::getUserId, accountId)
                .eq(DietCollect::getDietId, request.getDietId());
        dietCollectService.remove(wrapper);
        return ApiResponse.success();
    }
}

