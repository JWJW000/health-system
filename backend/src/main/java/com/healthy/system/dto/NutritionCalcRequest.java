package com.healthy.system.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class NutritionCalcRequest {

    @NotNull
    @Min(1)
    @Max(2)
    private Integer gender;

    @NotNull
    @Min(10)
    @Max(100)
    private Integer age;

    @NotNull
    @Min(100)
    @Max(250)
    private Double heightCm;

    @NotNull
    @Min(30)
    @Max(300)
    private Double weightKg;

    @NotNull
    @Min(1)
    @Max(4)
    private Integer fitnessGoal;
}

