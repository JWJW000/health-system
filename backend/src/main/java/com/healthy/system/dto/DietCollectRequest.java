package com.healthy.system.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DietCollectRequest {

    @NotNull
    private Long dietId;
}

