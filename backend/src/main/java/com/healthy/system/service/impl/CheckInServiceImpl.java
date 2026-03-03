package com.healthy.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthy.system.entity.CheckIn;
import com.healthy.system.mapper.CheckInMapper;
import com.healthy.system.service.CheckInService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CheckInServiceImpl extends ServiceImpl<CheckInMapper, CheckIn> implements CheckInService {

    @Override
    public boolean checkInToday(Long userId, LocalDate today) {
        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckIn::getUserId, userId)
                .eq(CheckIn::getCheckDate, today);
        CheckIn exist = this.getOne(wrapper, false);
        if (exist != null) {
            return false;
        }
        CheckIn record = new CheckIn();
        record.setUserId(userId);
        record.setCheckDate(today);
        this.save(record);
        return true;
    }

    @Override
    public long countByUser(Long userId) {
        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckIn::getUserId, userId);
        return this.count(wrapper);
    }

    @Override
    public List<CheckIn> history(Long userId) {
        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckIn::getUserId, userId)
                .orderByDesc(CheckIn::getCheckDate);
        return this.list(wrapper);
    }

    @Override
    public List<LocalDate> calendarDays(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckIn::getUserId, userId)
                .between(CheckIn::getCheckDate, start, end);
        return this.list(wrapper).stream()
                .map(CheckIn::getCheckDate)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> summary(Long userId) {
        Map<String, Object> map = new HashMap<>();
        long total = countByUser(userId);
        List<CheckIn> history = history(userId);
        map.put("total", total);
        map.put("recent", history.stream().limit(5).collect(Collectors.toList()));
        return map;
    }
}

