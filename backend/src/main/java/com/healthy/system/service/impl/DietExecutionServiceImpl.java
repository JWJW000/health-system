package com.healthy.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthy.system.entity.DietExecution;
import com.healthy.system.mapper.DietExecutionMapper;
import com.healthy.system.service.DietExecutionService;
import org.springframework.stereotype.Service;

@Service
public class DietExecutionServiceImpl extends ServiceImpl<DietExecutionMapper, DietExecution>
        implements DietExecutionService {
}

