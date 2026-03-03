package com.healthy.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.healthy.system.entity.CheckIn;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CheckInService extends IService<CheckIn> {

    boolean checkInToday(Long userId, LocalDate today);

    long countByUser(Long userId);

    List<CheckIn> history(Long userId);

    /**
     * 返回指定年月内已打卡日期的集合，用于日历高亮
     */
    List<LocalDate> calendarDays(Long userId, int year, int month);

    /**
     * 累计打卡与最近简要信息
     */
    Map<String, Object> summary(Long userId);
}

