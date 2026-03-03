package com.healthy.system.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class UserWeightLogRequest {

    private LocalDate logDate;

    @NotNull
    @Min(30)
    @Max(300)
    private Double weightKg;
}

