package com.healthy.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthy.system.entity.UserWeightLog;
import com.healthy.system.mapper.UserWeightLogMapper;
import com.healthy.system.service.UserWeightLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserWeightLogServiceImpl extends ServiceImpl<UserWeightLogMapper, UserWeightLog>
        implements UserWeightLogService {

    @Override
    public List<UserWeightLog> listRecent(Long userId, int days, LocalDate today) {
        LocalDate start = today.minusDays(days - 1L);
        LambdaQueryWrapper<UserWeightLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWeightLog::getUserId, userId)
                .between(UserWeightLog::getLogDate, start, today)
                .orderByAsc(UserWeightLog::getLogDate);
        return this.list(wrapper);
    }
}

