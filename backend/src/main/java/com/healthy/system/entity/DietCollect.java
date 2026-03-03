package com.healthy.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("diet_collect")
public class DietCollect {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long dietId;

    private LocalDateTime createdTime;
}

