package com.healthy.system.controller;

import com.healthy.system.common.ApiResponse;
import com.healthy.system.dto.NutritionCalcRequest;
import com.healthy.system.dto.NutritionCalcResponse;
import com.healthy.system.service.NutritionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/nutrition")
@Validated
public class NutritionController {

    private final NutritionService nutritionService;

    public NutritionController(NutritionService nutritionService) {
        this.nutritionService = nutritionService;
    }

    @PostMapping("/calc")
    public ApiResponse<NutritionCalcResponse> calculate(@Valid @RequestBody NutritionCalcRequest request) {
        NutritionCalcResponse resp = nutritionService.calculate(request);
        return ApiResponse.success(resp);
    }
}

