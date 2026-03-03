CREATE DATABASE IF NOT EXISTS healthy_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE healthy_system;

-- 用户基础信息
CREATE TABLE IF NOT EXISTS user_profile (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    username      VARCHAR(64) NOT NULL COMMENT '用户名（可选简单登录用）',
    gender        TINYINT     NOT NULL COMMENT '性别：1-男 2-女',
    age           INT         NOT NULL,
    height_cm     DECIMAL(5,2) NOT NULL COMMENT '身高cm',
    weight_kg     DECIMAL(5,2) NOT NULL COMMENT '当前体重kg',
    body_fat_pct  DECIMAL(5,2) NULL COMMENT '体脂率%',
    fitness_goal  TINYINT     NOT NULL COMMENT '健身目标：1-增肌 2-减脂 3-塑形 4-功能性训练',
    experience_years INT      NULL COMMENT '健身年限(年)',
    training_scene VARCHAR(128) NULL COMMENT '训练场景，如：健身房/家用器械',
    allergy_foods  VARCHAR(255) NULL COMMENT '过敏食材',
    created_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='用户基础信息';

-- 用户体重记录（支撑15天体重曲线）
CREATE TABLE IF NOT EXISTS user_weight_log (
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id   BIGINT      NOT NULL,
    log_date  DATE        NOT NULL,
    weight_kg DECIMAL(5,2) NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_date (user_id, log_date),
    CONSTRAINT fk_weight_user FOREIGN KEY (user_id) REFERENCES user_profile(id)
) COMMENT='用户体重记录';

-- 食谱模板
CREATE TABLE IF NOT EXISTS diet_template (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    fitness_type  TINYINT     NOT NULL COMMENT '适配健身目标：1-增肌 2-减脂 3-塑形 4-功能性',
    name          VARCHAR(128) NOT NULL COMMENT '食谱名称',
    total_calorie INT         NOT NULL COMMENT '总热量(千卡)',
    carb_grams    DECIMAL(6,2) NOT NULL COMMENT '碳水克数',
    protein_grams DECIMAL(6,2) NOT NULL COMMENT '蛋白质克数',
    fat_grams     DECIMAL(6,2) NOT NULL COMMENT '脂肪克数',
    meal_plan     TEXT        NOT NULL COMMENT '每日三餐+加餐明细',
    cooking_tips  TEXT        NULL COMMENT '烹饪建议',
    image_url     VARCHAR(255) NULL COMMENT '展示图片',
    created_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='饮食食谱模板';

-- 食谱收藏
CREATE TABLE IF NOT EXISTS diet_collect (
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id   BIGINT NOT NULL,
    diet_id   BIGINT NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_diet (user_id, diet_id),
    CONSTRAINT fk_collect_user FOREIGN KEY (user_id) REFERENCES user_profile(id),
    CONSTRAINT fk_collect_diet FOREIGN KEY (diet_id) REFERENCES diet_template(id)
) COMMENT='用户食谱收藏';

-- 训练动作
CREATE TABLE IF NOT EXISTS train_action (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    muscle_group VARCHAR(32) NOT NULL COMMENT '肌群：chest/back/leg/shoulder/core等',
    name         VARCHAR(128) NOT NULL COMMENT '动作名称',
    description  TEXT        NULL COMMENT '文字说明与注意事项',
    sets_suggest INT         NULL COMMENT '推荐组数',
    reps_suggest INT         NULL COMMENT '每组次数建议',
    weight_suggest VARCHAR(64) NULL COMMENT '重量建议说明',
    image_url    VARCHAR(255) NULL COMMENT '动作图片',
    video_url    VARCHAR(255) NULL COMMENT '动作视频',
    created_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='训练动作库';

-- 签到打卡
CREATE TABLE IF NOT EXISTS check_in (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL,
    check_date  DATE   NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_date (user_id, check_date),
    CONSTRAINT fk_checkin_user FOREIGN KEY (user_id) REFERENCES user_profile(id)
) COMMENT='签到打卡记录';

-- 系统用户账号
CREATE TABLE IF NOT EXISTS sys_user (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    username      VARCHAR(64) NOT NULL COMMENT '登录账号，唯一',
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt 加密密码',
    role          VARCHAR(16) NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
    profile_id    BIGINT NULL COMMENT '关联的用户档案ID',
    created_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    CONSTRAINT fk_sysuser_profile FOREIGN KEY (profile_id) REFERENCES user_profile(id)
) COMMENT='系统登录用户';

