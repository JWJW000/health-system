package com.healthy.system.service.impl;

import com.healthy.system.dto.NutritionCalcRequest;
import com.healthy.system.dto.NutritionCalcResponse;
import com.healthy.system.enums.FitnessGoalEnum;
import com.healthy.system.enums.GenderEnum;
import com.healthy.system.service.NutritionService;
import org.springframework.stereotype.Service;

@Service
public class NutritionServiceImpl implements NutritionService {

    @Override
    public NutritionCalcResponse calculate(NutritionCalcRequest request) {
        GenderEnum gender = GenderEnum.fromCode(request.getGender());
        FitnessGoalEnum goal = FitnessGoalEnum.fromCode(request.getFitnessGoal());

        double weight = request.getWeightKg();
        double height = request.getHeightCm();
        int age = request.getAge();

        // Mifflin-St Jeor 公式
        double bmr;
        if (gender == GenderEnum.MALE) {
            bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        } else {
            bmr = 10 * weight + 6.25 * height - 5 * age - 161;
        }

        // 根据目标估算每日总热量（简单系数/差额）
        double totalCalorie;
        double proteinPerKg;
        double carbPerKg;
        double fatPerKg;

        switch (goal) {
            case CUT:
                totalCalorie = bmr * 0.8;      // 略有热量缺口
                proteinPerKg = 2.0;
                carbPerKg = 3.0;
                fatPerKg = 0.8;
                break;
            case BULK:
                totalCalorie = bmr * 1.15;     // 略有热量盈余
                proteinPerKg = 1.8;
                carbPerKg = 5.0;
                fatPerKg = 1.0;
                break;
            case SHAPE:
                totalCalorie = bmr * 1.0;      // 基本持平
                proteinPerKg = 1.6;
                carbPerKg = 4.0;
                fatPerKg = 0.9;
                break;
            case FUNCTIONAL:
            default:
                totalCalorie = bmr * 1.05;
                proteinPerKg = 1.8;
                carbPerKg = 4.5;
                fatPerKg = 1.0;
                break;
        }

        double protein = roundOneDecimal(proteinPerKg * weight);
        double carb = roundOneDecimal(carbPerKg * weight);
        double fat = roundOneDecimal(fatPerKg * weight);

        NutritionCalcResponse resp = new NutritionCalcResponse();
        resp.setBmr(roundOneDecimal(bmr));
        resp.setTotalCalorie(roundOneDecimal(totalCalorie));
        resp.setProteinGrams(protein);
        resp.setCarbGrams(carb);
        resp.setFatGrams(fat);
        return resp;
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}

