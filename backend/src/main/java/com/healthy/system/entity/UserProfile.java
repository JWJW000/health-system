package com.healthy.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_profile")
public class UserProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private Integer gender;

    private Integer age;

    private Double heightCm;

    private Double weightKg;

    private Double bodyFatPct;

    private Integer fitnessGoal;

    private Integer experienceYears;

    private String trainingScene;

    private String allergyFoods;

    private LocalDateTime createdTime;

    private LocalDateTime updateTime;
}

