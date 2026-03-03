package com.healthy.system.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class UserProfileRequest {

    private Long id;

    @Size(max = 64)
    private String username;

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

    @Min(3)
    @Max(60)
    private Double bodyFatPct;

    @NotNull
    @Min(1)
    @Max(4)
    private Integer fitnessGoal;

    @Min(0)
    @Max(50)
    private Integer experienceYears;

    @Size(max = 128)
    private String trainingScene;

    @Size(max = 255)
    private String allergyFoods;
}

