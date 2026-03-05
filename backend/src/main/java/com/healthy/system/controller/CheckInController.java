package com.healthy.system.controller;

import com.healthy.system.auth.UserContext;
import com.healthy.system.common.ApiResponse;
import com.healthy.system.entity.SysUser;
import com.healthy.system.entity.CheckIn;
import com.healthy.system.entity.UserProfile;
import com.healthy.system.service.CheckInService;
import com.healthy.system.service.MinioService;
import com.healthy.system.service.SysUserService;
import com.healthy.system.service.UserProfileService;
import com.healthy.system.service.UserWeightLogService;
import com.healthy.system.entity.UserWeightLog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final CheckInService checkInService;
    private final SysUserService sysUserService;
    private final UserProfileService userProfileService;
    private final UserWeightLogService userWeightLogService;
    private final MinioService minioService;

    public CheckInController(CheckInService checkInService,
                             SysUserService sysUserService,
                             UserProfileService userProfileService,
                             UserWeightLogService userWeightLogService,
                             MinioService minioService) {
        this.checkInService = checkInService;
        this.sysUserService = sysUserService;
        this.userProfileService = userProfileService;
        this.userWeightLogService = userWeightLogService;
        this.minioService = minioService;
    }

    /**
     * 今日打卡，可选传体重(weightKg)与健身图(file)
     */
    @PostMapping
    public ApiResponse<String> checkIn(
            @RequestParam(required = false) Double weightKg,
            @RequestParam(required = false) MultipartFile file) {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.error(401, "未登录");
        }
        SysUser account = sysUserService.getById(accountId);
        if (account == null || account.getProfileId() == null) {
            return ApiResponse.error(400, "请先完善个人信息");
        }
        Long profileId = account.getProfileId();
        LocalDate today = LocalDate.now();

        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            try {
                imageUrl = minioService.uploadCheckInImage(file, profileId, today);
            } catch (IllegalArgumentException e) {
                // 比如健身图片过大或格式错误，直接把原因返回给前端
                return ApiResponse.error(400, e.getMessage());
            }
        }

        boolean ok = checkInService.checkInToday(profileId, today, weightKg, imageUrl);
        if (!ok) {
            return ApiResponse.error(1, "今日已打卡");
        }
        if (weightKg != null) {
            UserProfile profile = userProfileService.getById(profileId);
            if (profile != null) {
                profile.setWeightKg(weightKg);
                userProfileService.updateById(profile);
            }
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

    /**
     * 周报：本周打卡统计、最长连续打卡天数、体重变化与简单勋章
     */
    @GetMapping("/weekly-report")
    public ApiResponse<Map<String, Object>> weeklyReport(@RequestParam(required = false) String weekStart) {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.error(401, "未登录");
        }
        SysUser account = sysUserService.getById(accountId);
        if (account == null || account.getProfileId() == null) {
            return ApiResponse.error(400, "请先完善个人信息");
        }
        Long profileId = account.getProfileId();

        LocalDate start;
        if (weekStart != null && !weekStart.isEmpty()) {
            start = LocalDate.parse(weekStart);
        } else {
            // 默认当前周，从周一开始
            LocalDate today = LocalDate.now();
            start = today.minusDays((today.getDayOfWeek().getValue() + 6) % 7);
        }
        LocalDate end = start.plusDays(6);

        // 本周打卡记录
        List<CheckIn> allHistory = checkInService.history(profileId);
        long weekCheckins = allHistory.stream()
                .map(CheckIn::getCheckDate)
                .filter(d -> !d.isBefore(start) && !d.isAfter(end))
                .count();

        // 最长连续打卡天数（历史整体）
        int longestStreak = 0;
        int currentStreak = 0;
        LocalDate prevDate = null;
        List<LocalDate> sortedDates = allHistory.stream()
                .map(CheckIn::getCheckDate)
                .sorted()
                .collect(Collectors.toList());
        for (LocalDate d : sortedDates) {
            if (prevDate == null || d.isEqual(prevDate.plusDays(1))) {
                currentStreak++;
            } else if (!d.isEqual(prevDate)) {
                currentStreak = 1;
            }
            if (currentStreak > longestStreak) {
                longestStreak = currentStreak;
            }
            prevDate = d;
        }

        // 本周体重变化（如有记录）
        List<UserWeightLog> weightLogs = userWeightLogService.listRecent(profileId, 60, LocalDate.now());
        Double startWeight = null;
        Double endWeight = null;
        for (UserWeightLog log : weightLogs) {
            LocalDate d = log.getLogDate();
            if (!d.isAfter(start)) {
                startWeight = log.getWeightKg();
            }
            if (!d.isAfter(end)) {
                endWeight = log.getWeightKg();
            }
        }
        Double weightDiff = null;
        if (startWeight != null && endWeight != null) {
            weightDiff = endWeight - startWeight;
        }

        // 简单勋章规则
        String badge = null;
        String badgeText = null;
        String nextBadgeHint = null;
        if (longestStreak >= 30) {
            badge = "IRON_30";
            badgeText = "铁人30天";
        } else if (longestStreak >= 7) {
            badge = "STREAK_7";
            badgeText = "坚持7天";
            nextBadgeHint = "再坚持 " + (30 - longestStreak) + " 天可获得 30 天徽章";
        } else if (longestStreak > 0) {
            nextBadgeHint = "连续打卡达到 7 天可获得首个坚持徽章";
        }

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("weekStart", start);
        result.put("weekEnd", end);
        result.put("weekCheckins", weekCheckins);
        result.put("longestStreak", longestStreak);
        result.put("startWeight", startWeight);
        result.put("endWeight", endWeight);
        result.put("weightDiff", weightDiff);
        result.put("badge", badge);
        result.put("badgeText", badgeText);
        result.put("nextBadgeHint", nextBadgeHint);
        return ApiResponse.success(result);
    }
}

