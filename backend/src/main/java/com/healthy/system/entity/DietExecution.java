package com.healthy.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("diet_execution")
public class DietExecution {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private java.time.LocalDate execDate;

    private Integer followed;

    private LocalDateTime createdTime;
}

