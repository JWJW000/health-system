package com.healthy.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.healthy.system.auth.UserContext;
import com.healthy.system.common.ApiResponse;
import com.healthy.system.dto.DietCollectRequest;
import com.healthy.system.dto.NutritionCalcRequest;
import com.healthy.system.dto.NutritionCalcResponse;
import com.healthy.system.entity.DietCollect;
import com.healthy.system.entity.DietTemplate;
import com.healthy.system.entity.DietExecution;
import com.healthy.system.entity.SysUser;
import com.healthy.system.entity.UserProfile;
import com.healthy.system.service.DietCollectService;
import com.healthy.system.service.DietTemplateService;
import com.healthy.system.service.DietExecutionService;
import com.healthy.system.service.NutritionService;
import com.healthy.system.service.SysUserService;
import com.healthy.system.service.UserProfileService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diet")
@Validated
public class DietController {

    private final DietTemplateService dietTemplateService;
    private final DietCollectService dietCollectService;
    private final SysUserService sysUserService;
    private final UserProfileService userProfileService;
    private final NutritionService nutritionService;
    private final DietExecutionService dietExecutionService;

    public DietController(DietTemplateService dietTemplateService,
                          DietCollectService dietCollectService,
                          SysUserService sysUserService,
                          UserProfileService userProfileService,
                          NutritionService nutritionService,
                          DietExecutionService dietExecutionService) {
        this.dietTemplateService = dietTemplateService;
        this.dietCollectService = dietCollectService;
        this.sysUserService = sysUserService;
        this.userProfileService = userProfileService;
        this.nutritionService = nutritionService;
        this.dietExecutionService = dietExecutionService;
    }

    /**
     * 基于用户健身目标返回推荐食谱列表
     */
    @GetMapping("/list")
    public ApiResponse<List<DietTemplate>> listByUser() {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.error(401, "未登录");
        }
        SysUser account = sysUserService.getById(accountId);
        if (account == null || account.getProfileId() == null) {
            return ApiResponse.success(List.of());
        }
        List<DietTemplate> list = dietTemplateService.listByUserGoal(account.getProfileId());
        return ApiResponse.success(list);
    }

    /**
     * 生成“今日饮食计划”视图：基于当前用户目标与热量需求，从多份模板中选出适配度最高的若干份
     */
    @GetMapping("/today")
    public ApiResponse<Map<String, Object>> todayPlan() {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.error(401, "未登录");
        }
        SysUser account = sysUserService.getById(accountId);
        if (account == null || account.getProfileId() == null) {
            return ApiResponse.error(400, "请先完善个人信息");
        }
        Long profileId = account.getProfileId();
        UserProfile profile = userProfileService.getById(profileId);
        if (profile == null) {
            return ApiResponse.error(400, "请先完善个人信息");
        }

        // 计算用户当日推荐总热量
        NutritionCalcRequest req = new NutritionCalcRequest();
        req.setGender(profile.getGender());
        req.setAge(profile.getAge());
        req.setHeightCm(profile.getHeightCm());
        req.setWeightKg(profile.getWeightKg());
        req.setFitnessGoal(profile.getFitnessGoal());
        NutritionCalcResponse calc = nutritionService.calculate(req);
        double targetCalorie = calc.getTotalCalorie() != null ? calc.getTotalCalorie() : 0D;

        List<DietTemplate> list = dietTemplateService.listByUserGoal(profileId);
        if (list.isEmpty()) {
            return ApiResponse.success(Map.of(
                    "templates", List.of(),
                    "targetCalorie", targetCalorie,
                    "note", "暂无适配当前目标的食谱模板，请先在后台配置。"
            ));
        }

        // 计算每个模板的适配度分数，按总热量与目标热量差、适合水平等排序
        int expYears = profile.getExperienceYears() != null ? profile.getExperienceYears() : 0;
        list.sort((a, b) -> {
            double sa = scoreTemplate(a, targetCalorie, expYears);
            double sb = scoreTemplate(b, targetCalorie, expYears);
            return Double.compare(sa, sb);
        });

        List<Map<String, Object>> candidates = new java.util.ArrayList<>();
        int limit = Math.min(3, list.size());
        for (int i = 0; i < limit; i++) {
            DietTemplate template = list.get(i);

            Map<String, String> meals = new HashMap<>();
            if (template.getMealPlan() != null) {
                String[] lines = template.getMealPlan().split("\\r?\\n");
                StringBuilder other = new StringBuilder();
                for (String raw : lines) {
                    String line = raw.trim();
                    if (line.isEmpty()) {
                        continue;
                    }
                    if (line.startsWith("早餐")) {
                        meals.put("breakfast", line);
                    } else if (line.startsWith("午餐")) {
                        meals.put("lunch", line);
                    } else if (line.startsWith("晚餐")) {
                        meals.put("dinner", line);
                    } else if (line.startsWith("加餐")) {
                        meals.put("snack", line);
                    } else {
                        if (other.length() > 0) {
                            other.append(" / ");
                        }
                        other.append(line);
                    }
                }
                if (other.length() > 0 && !meals.containsKey("others")) {
                    meals.put("others", other.toString());
                }
            }

            List<String> shoppingList = new java.util.ArrayList<>();
            for (Map.Entry<String, String> entry : meals.entrySet()) {
                String text = entry.getValue();
                String cleaned = text.replace("早餐：", "")
                        .replace("午餐：", "")
                        .replace("晚餐：", "")
                        .replace("加餐：", "")
                        .trim();
                if (!cleaned.isEmpty()) {
                    shoppingList.add(cleaned);
                }
            }

            Map<String, Object> candidate = new HashMap<>();
            candidate.put("template", template);
            candidate.put("meals", meals);
            candidate.put("shoppingList", shoppingList);
            candidate.put("score", scoreTemplate(template, targetCalorie, expYears));
            candidates.add(candidate);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("templates", candidates);
        result.put("targetCalorie", targetCalorie);
        if (!candidates.isEmpty()) {
            result.put("template", candidates.get(0).get("template"));
            result.put("meals", candidates.get(0).get("meals"));
            result.put("shoppingList", candidates.get(0).get("shoppingList"));
        }
        result.put("note", "根据你的健身目标与热量需求为你选取了推荐食谱，可作为今日的一日饮食参考。");
        return ApiResponse.success(result);
    }

    private double scoreTemplate(DietTemplate template, double targetCalorie, int expYears) {
        double calorie = template.getTotalCalorie() != null ? template.getTotalCalorie() : 0D;
        double diff = Math.abs(calorie - targetCalorie);
        double score = diff;

        // 新手优先 NEWBIE，其次 INTERMEDIATE
        if (expYears <= 1) {
            String level = template.getSuitableLevel();
            if ("NEWBIE".equalsIgnoreCase(level)) {
                score *= 0.8;
            } else if ("INTERMEDIATE".equalsIgnoreCase(level)) {
                score *= 0.9;
            }
        }
        return score;
    }

    /**
     * 单个食谱详情
     */
    @GetMapping("/{id}")
    public ApiResponse<DietTemplate> detail(@PathVariable Long id) {
        DietTemplate template = dietTemplateService.getById(id);
        return ApiResponse.success(template);
    }

    /**
     * 收藏
     */
    @PostMapping("/collect")
    public ApiResponse<Void> collect(@Valid @RequestBody DietCollectRequest request) {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.error(401, "未登录");
        }
        LambdaQueryWrapper<DietCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DietCollect::getUserId, accountId)
                .eq(DietCollect::getDietId, request.getDietId());
        DietCollect exist = dietCollectService.getOne(wrapper, false);
        if (exist == null) {
            DietCollect collect = new DietCollect();
            collect.setUserId(accountId);
            collect.setDietId(request.getDietId());
            dietCollectService.save(collect);
        }
        return ApiResponse.success();
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/collect")
    public ApiResponse<Void> cancelCollect(@Valid @RequestBody DietCollectRequest request) {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.error(401, "未登录");
        }
        LambdaQueryWrapper<DietCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DietCollect::getUserId, accountId)
                .eq(DietCollect::getDietId, request.getDietId());
        dietCollectService.remove(wrapper);
        return ApiResponse.success();
    }

    /**
     * 记录今日饮食执行情况（是否大致按推荐饮食执行）
     */
    @PostMapping("/execute-today")
    public ApiResponse<Void> executeToday(@RequestParam(defaultValue = "1") int followed) {
        Long accountId = UserContext.getUserId();
        if (accountId == null) {
            return ApiResponse.error(401, "未登录");
        }
        SysUser account = sysUserService.getById(accountId);
        if (account == null || account.getProfileId() == null) {
            return ApiResponse.error(400, "请先完善个人信息");
        }
        Long profileId = account.getProfileId();
        java.time.LocalDate today = java.time.LocalDate.now();

        LambdaQueryWrapper<DietExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DietExecution::getUserId, profileId)
                .eq(DietExecution::getExecDate, today);
        DietExecution exist = dietExecutionService.getOne(wrapper, false);
        if (exist == null) {
            exist = new DietExecution();
            exist.setUserId(profileId);
            exist.setExecDate(today);
        }
        exist.setFollowed(followed != 0 ? 1 : 0);
        dietExecutionService.saveOrUpdate(exist);
        return ApiResponse.success();
    }
}

