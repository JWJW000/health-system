package com.healthy.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("train_action")
public class TrainAction {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String muscleGroup;

    private String name;

    private String description;

    private Integer setsSuggest;

    private Integer repsSuggest;

    private String weightSuggest;

    private String imageUrl;

    private String videoUrl;

    private Integer difficulty;

    private String equipment;

    private Integer durationMinutes;

    private LocalDateTime createdTime;
}

