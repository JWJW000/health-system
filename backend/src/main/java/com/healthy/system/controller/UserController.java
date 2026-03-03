package com.healthy.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.healthy.system.auth.UserContext;
import com.healthy.system.common.ApiResponse;
import com.healthy.system.dto.UserProfileRequest;
import com.healthy.system.dto.UserWeightLogRequest;
import com.healthy.system.entity.SysUser;
import com.healthy.system.entity.UserProfile;
import com.healthy.system.entity.UserWeightLog;
import com.healthy.system.service.SysUserService;
import com.healthy.system.service.UserProfileService;
import com.healthy.system.service.UserWeightLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@Validated
public class UserController {

    private final UserProfileService userProfileService;
    private final UserWeightLogService weightLogService;
    private final SysUserService sysUserService;

    public UserController(UserProfileService userProfileService,
                          UserWeightLogService weightLogService,
                          SysUserService sysUserService) {
        this.userProfileService = userProfileService;
        this.weightLogService = weightLogService;
        this.sysUserService = sysUserService;
    }

    @PostMapping("/profile")
    public ApiResponse<UserProfile> saveOrUpdateProfile(@Valid @RequestBody UserProfileRequest request) {
        UserProfile profile = new UserProfile();
        BeanUtils.copyProperties(request, profile);
        userProfileService.saveOrUpdate(profile);

        Long accountId = UserContext.getUserId();
        if (accountId != null) {
            SysUser account = sysUserService.getById(accountId);
            if (account != null) {
                account.setProfileId(profile.getId());
                sysUserService.updateById(account);
            }
        }
        return ApiResponse.success(profile);
    }

    @GetMapping("/profile/me")
    public ApiResponse<UserProfile> getProfile() {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.success(null);
        }
        SysUser account = sysUserService.getById(accountId);
        if (account == null || account.getProfileId() == null) {
            return ApiResponse.success(null);
        }
        UserProfile profile = userProfileService.getById(account.getProfileId());
        return ApiResponse.success(profile);
    }

    @PostMapping("/weight")
    public ApiResponse<Void> saveWeight(@Valid @RequestBody UserWeightLogRequest request) {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.error(401, "未登录");
        }
        SysUser account = sysUserService.getById(accountId);
        if (account == null || account.getProfileId() == null) {
            return ApiResponse.error(400, "请先完善个人信息");
        }
        Long profileId = account.getProfileId();
        UserWeightLog log = new UserWeightLog();
        log.setUserId(profileId);
        log.setWeightKg(request.getWeightKg());
        log.setLogDate(request.getLogDate() != null ? request.getLogDate() : LocalDate.now());
        weightLogService.saveOrUpdate(log,
                new LambdaQueryWrapper<UserWeightLog>()
                        .eq(UserWeightLog::getUserId, log.getUserId())
                        .eq(UserWeightLog::getLogDate, log.getLogDate()));
        return ApiResponse.success();
    }

    @GetMapping("/weight/recent")
    public ApiResponse<List<UserWeightLog>> recentWeights(@RequestParam(defaultValue = "15") int days) {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.error(401, "未登录");
        }
        SysUser account = sysUserService.getById(accountId);
        if (account == null || account.getProfileId() == null) {
            return ApiResponse.success(List.of());
        }
        Long profileId = account.getProfileId();
        List<UserWeightLog> list = weightLogService.listRecent(profileId, days, LocalDate.now());
        return ApiResponse.success(list);
    }
}

