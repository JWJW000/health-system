# 健康管理系统（Healthy System）

面向健身用户的个性化饮食与训练系统，包含用户端与管理员后台。

---

## 一、整体业务流程

### 账号注册与登录
- 用户通过**用户名 + 密码**注册，支持普通用户（USER）与管理员（ADMIN）角色。
- 登录成功后下发 **JWT** 令牌，前端存储在 localStorage。
- **管理员单独入口**：访问 `/admin` 进入管理员登录页，仅 ADMIN 角色可访问后台路由。

### 首次建档引导
- 首次登录时，在**个人信息**页面触发**两步向导式表单**：
  - **第一步（必填）**：性别、年龄、身高、体重、健身目标（增肌 / 减脂 / 塑形 / 功能性训练）；
  - **第二步（选填）**：体脂率、健身年限、训练场景、过敏食材、昵称。
- 提交后自动跳转首页「今日概览」，完成“建档 → 看到计划”的闭环。

### 日常使用主链路
- 用户通过首页查看：今日基础代谢、推荐总热量与三大营养素；今日训练日提示与推荐主练肌群；今日饮食一日菜单（早餐/午餐/晚餐/加餐）；本周打卡概览与周报入口。
- 完成训练与饮食后：在**打卡**页一键打卡（可选填体重、上传健身图片）；在首页「今日饮食建议」中点击「今天大致按这个吃了」记录饮食执行。
- 通过**周报**页面查看一周打卡、连续打卡、体重变化与徽章状态。

### 后台运营
- 管理员登录后可维护：**食谱模板**（营养配比、标签、适合水平、预算档）；**训练动作库**（肌群、难度、器械、说明）；**用户列表**（查看用户与角色、修改角色）。后台首页**仪表盘**展示注册用户数、食谱模板数、训练动作数。

---

## 二、前台主要功能模块

### 1. 账号与权限

| 功能       | 路由     | 说明 |
|------------|----------|------|
| 登录/注册  | `/login` | 用户名+密码登录/注册；「忘记密码？」入口提示联系管理员，预留扩展。 |
| 账号设置   | `/account` | 修改密码（原密码 + 新密码 + 确认），成功后清空表单并提示。 |

### 2. 个人信息与体重管理

| 功能           | 路由      | 说明 |
|----------------|-----------|------|
| 个人档案       | `/profile` | 必填：性别、年龄、身高、当前体重、健身目标；选填：体脂率、健身年限、训练场景、过敏食材、昵称。首次为向导模式（2 步），非首次为普通编辑表单。支持**头像上传**（MinIO）。 |
| 体重记录与曲线 | 同上      | 在 Profile 页记录每日体重（默认当天）；ECharts 绘制最近 15 天体重曲线。 |

### 3. 营养计算与饮食推荐

| 功能               | 路由    | 说明 |
|--------------------|---------|------|
| 基础代谢与推荐热量 | 首页等  | Mifflin-St Jeor 公式计算 BMR，按健身目标调整推荐总热量；按体重与目标计算三大营养素（碳水/蛋白/脂肪）。 |
| 今日饮食计划       | 首页    | 基于用户档案与 diet_template 生成目标热量下多套候选方案，拆分为早餐/午餐/晚餐/加餐；展示总热量、三大营养素、烹饪建议、采买清单；「换一个方案」本地切换；「今天大致按这个吃了」记录饮食执行（diet_execution）。 |
| 食谱推荐列表       | `/diet` | 按健身目标从 diet_template 返回列表；展示名称、热量、三大营养素、与目标热量的适配度；支持收藏/取消收藏（diet_collect）。 |

### 4. 训练计划与动作指导

| 功能                     | 路由     | 说明 |
|--------------------------|----------|------|
| 本周训练计划 + 当日推荐   | `/train` | 左侧按肌群（胸/背/腿/肩/核心）浏览动作（组数/次数/重量/说明/教学视频）；右上「本周训练计划」7 格展示每日训练类型/主肌群，支持切换周、查看当日 4~6 个推荐动作；右下「有氧训练建议」按目标返回类型、时长、频次、心率区间。训练动作详情中可直接播放绑定的教学视频，帮助用户规范动作。 |
| 首页今日训练提示         | 首页     | 根据 today-plan 判断是否训练日，展示主练肌群或休息/机动日建议。 |

### 5. 打卡与周报/激励

| 功能         | 路由           | 说明 |
|--------------|----------------|------|
| 日打卡       | `/checkin`     | 一键打卡；可选填写**当日体重**、上传**健身图片**（MinIO）；ECharts 日历热力图展示月度打卡，支持切换月份。 |
| 首页本周概览 | 首页           | 最近 7 天打卡次数、三档提示文案；总打卡天数与 BMR、总热量、三大营养素等。 |
| 周报与徽章   | `/weekly-report` | 本周（周一~周日）打卡天数；历史最长连续打卡；最近 60 天体重变化（周初/周末体重与差值）；徽章：STREAK_7（连续≥7 天）、IRON_30（连续≥30 天）及下一目标提示。 |

---

## 三、后台管理模块

| 模块           | 路由                   | 说明 |
|----------------|------------------------|------|
| 仪表盘         | `/admin/dashboard`     | 统计注册用户数、食谱模板数、训练动作数，并跳转各运营模块。 |
| 食谱管理       | `/admin/diets`         | 按健身目标展示食谱模板；新建/编辑：适配目标、名称、热量、三大营养素、mealPlan、烹饪建议、tags、suitableLevel、estimatedCostLevel、image_url。 |
| 训练动作库     | `/admin/train-actions`  | 列表展示动作名称、肌群、组数/次数/重量、说明；新建/编辑：肌群、难度、器械、建议时长、image_url、video_url。 |
| 用户管理       | `/admin/users`         | 列表展示用户 ID、用户名、角色、创建时间；按用户名关键字搜索；**修改用户角色**（USER/ADMIN）。 |

---

## 四、账号体系与安全

### 数据结构（核心表）
- **sys_user**：username、password_hash、role（USER/ADMIN）、email/phone（预留）、status（NORMAL/BANNED）、profile_id 关联 user_profile。
- **user_profile**：性别、年龄、身高、体重、体脂率、健身目标、年限、场景、过敏食材、昵称、**avatar_url**（MinIO）。
- **user_weight_log**：用户体重记录（支撑 15 天曲线）。
- **diet_template**：食谱模板；**diet_collect**：用户收藏；**diet_execution**：每日是否按推荐饮食执行。
- **train_action**：训练动作库，包含动作图片与教学视频 URL（image_url、video_url）。
- **check_in**：打卡记录，含 **weight_kg**、**image_url**（可选）。

### 接口能力（摘要）
- 注册：`POST /api/auth/register`
- 登录：`POST /api/auth/login`（返回 JWT 与角色）
- 修改密码：`POST /api/auth/change-password`（需登录，校验原密码）
- 个人档案：`POST /api/user/profile`、`GET /api/user/profile/me`
- 体重：`POST /api/user/weight`、`GET /api/user/weight/recent`
- **头像上传**：`POST /api/upload/avatar`（multipart/form-data，file）
- **训练动作图片上传**：`POST /api/upload/train/image`（multipart/form-data，file，返回 image_url，供 `train_action.image_url` 使用）
- **训练动作视频上传**：`POST /api/upload/train/video`（multipart/form-data，file，返回 video_url，供 `train_action.video_url` 使用）
- 营养计算：`POST /api/nutrition/calc`
- 饮食：`GET /api/diet/list`、`GET /api/diet/today`、`GET /api/diet/{id}`、收藏 `POST/DELETE /api/diet/collect`、**今日执行** `POST /api/diet/execute-today`
- 训练：`GET /api/train/actions`、`GET /api/train/today-plan`、`GET /api/train/plan`、`GET /api/train/week-plan`、`GET /api/train/day-detail`、`GET /api/train/aerobic`
- 打卡：`POST /api/checkin`（可选 weightKg、file）、`GET /api/checkin/summary`、`GET /api/checkin/history`、`GET /api/checkin/calendar`、`GET /api/checkin/weekly-report`
- 管理端：`GET /api/admin/user/list`、`POST /api/admin/user/role`；`GET/POST/PUT/DELETE /api/admin/diet/templates`；`GET/POST/PUT/DELETE /api/admin/train/actions`

---

## 五、技术与架构要点

### 前端
- **Vue 3** + `<script setup>` + **TypeScript**
- **Vue Router**：路由守卫基于 JWT 与本地用户信息（含角色）控制访问
- **ECharts**：体重曲线、打卡日历热力图
- **Element Plus**：表单、表格等组件
- **Vite** 构建

### 后端
- **Spring Boot 2.7** + **MyBatis-Plus**，分层：Controller / Service / Mapper / Entity / DTO
- **JWT** 鉴权，拦截器校验登录状态与管理员角色
- **MinIO**：头像上传（`/api/upload/avatar`）、打卡健身图片上传（打卡接口 `file` 参数）、训练动作图片与教学视频上传（`POST /api/upload/train/image`、`POST /api/upload/train/video`），配置见 `application.yml`（endpoint、bucket、大小限制等）
- 数据库：**MySQL**，表结构见 `backend/sql/schema.sql`

### 运行与配置
- **后端**：`backend` 目录，端口 **8080**，依赖 MySQL、MinIO（头像与打卡图片）。数据库名 `healthy_system`，脚本在 `backend/sql/schema.sql`。
- **前端**：`frontend` 目录，`pnpm install` 后 `pnpm dev`，通过 Vite 代理或配置请求后端 `http://localhost:8080`。
- **MinIO**：默认 `http://localhost:9000`，需创建 bucket（如 `healthy-avatars`），与 `application.yml` 中 `minio` 配置一致；上传限制（如 5MB/文件）在 Spring multipart 中配置。

---

以上为当前系统功能与架构说明，覆盖账号、档案、体重、饮食推荐与执行、训练计划、打卡（含体重与图片）、周报徽章、管理后台（仪表盘、食谱、动作库、用户与角色）及 MinIO 文件上传。
