package com.healthy.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("keypoint_annotation")
public class KeypointAnnotation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long actionId;
    private String keypointType;
    private BigDecimal xRatio;
    private BigDecimal yRatio;
    private Integer standardAngle;
    private Integer angleTolerance;
    private String color;
    private String description;
    private Integer showArc;
    private Integer showRange;
    private Integer showTrail;
    private LocalDateTime createdTime;
}
