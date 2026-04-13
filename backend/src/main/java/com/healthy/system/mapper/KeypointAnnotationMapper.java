package com.healthy.system.mapper;

import com.healthy.system.entity.KeypointAnnotation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface KeypointAnnotationMapper extends BaseMapper<KeypointAnnotation> {
    List<KeypointAnnotation> selectByActionId(Long actionId);
}
