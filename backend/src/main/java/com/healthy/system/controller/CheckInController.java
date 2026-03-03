package com.healthy.system.controller;

import com.healthy.system.auth.UserContext;
import com.healthy.system.common.ApiResponse;
import com.healthy.system.entity.SysUser;
import com.healthy.system.entity.CheckIn;
import com.healthy.system.service.CheckInService;
import com.healthy.system.service.SysUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final CheckInService checkInService;
        private final SysUserService sysUserService;

        public CheckInController(CheckInService checkInService, SysUserService sysUserService) {
        this.checkInService = checkInService;
            this.sysUserService = sysUserService;
    }

    /**
     * 今日打卡
     */
    @PostMapping
    public ApiResponse<String> checkIn() {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.error(401, "未登录");
        }
        SysUser account = sysUserService.getById(accountId);
        if (account == null || account.getProfileId() == null) {
            return ApiResponse.error(400, "请先完善个人信息");
        }
        Long profileId = account.getProfileId();
        boolean ok = checkInService.checkInToday(profileId, LocalDate.now());
        if (!ok) {
            return ApiResponse.error(1, "今日已打卡");
        }
        return ApiResponse.success("打卡成功");
    }

    /**
     * 累计打卡与最近摘要
     */
    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary() {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.error(401, "未登录");
        }
        SysUser account = sysUserService.getById(accountId);
        if (account == null || account.getProfileId() == null) {
            return ApiResponse.success(Map.of("total", 0, "recent", List.of()));
        }
        Long profileId = account.getProfileId();
        Map<String, Object> result = checkInService.summary(profileId);
        return ApiResponse.success(result);
    }

    /**
     * 历史记录列表
     */
    @GetMapping("/history")
    public ApiResponse<List<CheckIn>> history() {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.error(401, "未登录");
        }
        SysUser account = sysUserService.getById(accountId);
        if (account == null || account.getProfileId() == null) {
            return ApiResponse.success(List.of());
        }
        Long profileId = account.getProfileId();
        List<CheckIn> list = checkInService.history(profileId);
        return ApiResponse.success(list);
    }

    /**
     * 日历高亮所需日期数组
     */
    @GetMapping("/calendar")
    public ApiResponse<List<LocalDate>> calendar(@RequestParam int year,
                                                 @RequestParam int month) {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.error(401, "未登录");
        }
        SysUser account = sysUserService.getById(accountId);
        if (account == null || account.getProfileId() == null) {
            return ApiResponse.success(List.of());
        }
        Long profileId = account.getProfileId();
        List<LocalDate> days = checkInService.calendarDays(profileId, year, month);
        return ApiResponse.success(days);
    }
}

