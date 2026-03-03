package com.healthy.system.dto;

import lombok.Data;

@Data
public class NutritionCalcResponse {

    /**
     * 基础代谢率 (kcal)
     */
    private Double bmr;

    /**
     * 每日总推荐热量 (kcal)，可基于BMR和目标估算
     */
    private Double totalCalorie;

    private Double carbGrams;

    private Double proteinGrams;

    private Double fatGrams;
}

