package com.healthy.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthy.system.entity.DietCollect;
import com.healthy.system.mapper.DietCollectMapper;
import com.healthy.system.service.DietCollectService;
import org.springframework.stereotype.Service;

@Service
public class DietCollectServiceImpl extends ServiceImpl<DietCollectMapper, DietCollect>
        implements DietCollectService {
}

