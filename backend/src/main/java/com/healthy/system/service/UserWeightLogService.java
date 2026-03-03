package com.healthy.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.healthy.system.entity.UserWeightLog;

import java.time.LocalDate;
import java.util.List;

public interface UserWeightLogService extends IService<UserWeightLog> {

    /**
     * 查询最近若干天的体重记录，按日期升序
     */
    List<UserWeightLog> listRecent(Long userId, int days, LocalDate today);
}

