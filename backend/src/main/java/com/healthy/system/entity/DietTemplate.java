package com.healthy.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("diet_template")
public class DietTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer fitnessType;

    private String name;

    private Integer totalCalorie;

    private Double carbGrams;

    private Double proteinGrams;

    private Double fatGrams;

    private String mealPlan;

    private String cookingTips;

    private String imageUrl;

    private LocalDateTime createdTime;
}

