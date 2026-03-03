package com.healthy.system.service;

import com.healthy.system.dto.NutritionCalcRequest;
import com.healthy.system.dto.NutritionCalcResponse;

public interface NutritionService {

    NutritionCalcResponse calculate(NutritionCalcRequest request);
}

