CREATE DATABASE IF NOT EXISTS healthy_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE healthy_system;

-- 用户基础信息
CREATE TABLE IF NOT EXISTS user_profile (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    username      VARCHAR(64) NOT NULL COMMENT '用户名（可选简单登录用）',
    avatar_url    VARCHAR(255) NULL COMMENT '头像 URL（MinIO 等）',
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
    name              VARCHAR(128) NOT NULL COMMENT '食谱名称',
    total_calorie     INT         NOT NULL COMMENT '总热量(千卡)',
    carb_grams        DECIMAL(6,2) NOT NULL COMMENT '碳水克数',
    protein_grams     DECIMAL(6,2) NOT NULL COMMENT '蛋白质克数',
    fat_grams         DECIMAL(6,2) NOT NULL COMMENT '脂肪克数',
    meal_plan         TEXT        NOT NULL COMMENT '每日三餐+加餐明细',
    cooking_tips      TEXT        NULL COMMENT '烹饪建议',
    image_url         VARCHAR(255) NULL COMMENT '展示图片',
    tags              VARCHAR(255) NULL COMMENT '标签，如：上班族,低预算,中餐 等',
    suitable_level    VARCHAR(32) NULL COMMENT '适合水平：NEWBIE/INTERMEDIATE/ADVANCED',
    estimated_cost_level VARCHAR(16) NULL COMMENT '预算档位：LOW/MEDIUM/HIGH',
    created_time      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
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

-- 饮食执行记录（是否按推荐饮食执行）
CREATE TABLE IF NOT EXISTS diet_execution (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL COMMENT 'user_profile.id',
    exec_date   DATE   NOT NULL,
    followed    TINYINT NOT NULL DEFAULT 1 COMMENT '是否大致按推荐饮食执行：1-是 0-否',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_exec_date (user_id, exec_date),
    CONSTRAINT fk_diet_exec_user FOREIGN KEY (user_id) REFERENCES user_profile(id)
) COMMENT='饮食执行记录';

-- 训练动作
CREATE TABLE IF NOT EXISTS train_action (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    muscle_group VARCHAR(32) NOT NULL COMMENT '肌群：chest/back/leg/shoulder/core等',
    name            VARCHAR(128) NOT NULL COMMENT '动作名称',
    description     TEXT        NULL COMMENT '文字说明与注意事项',
    sets_suggest    INT         NULL COMMENT '推荐组数',
    reps_suggest    INT         NULL COMMENT '每组次数建议',
    weight_suggest  VARCHAR(64) NULL COMMENT '重量建议说明',
    image_url       VARCHAR(255) NULL COMMENT '动作图片',
    video_url       VARCHAR(255) NULL COMMENT '动作视频',
    difficulty      TINYINT     NULL COMMENT '难度等级：1-入门 2-中级 3-高级',
    equipment       VARCHAR(32) NULL COMMENT '器械要求：BODYWEIGHT/DUMBBELL/BARBELL/MACHINE',
    duration_minutes INT        NULL COMMENT '建议时长（分钟）',
    created_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='训练动作库';

-- 签到打卡
CREATE TABLE IF NOT EXISTS check_in (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL,
    check_date  DATE   NOT NULL,
    weight_kg   DECIMAL(5,2) NULL COMMENT '当日体重 kg',
    image_url   VARCHAR(255) NULL COMMENT '健身图片 URL',
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
    email         VARCHAR(128) NULL COMMENT '邮箱，用于找回密码等',
    phone         VARCHAR(32)  NULL COMMENT '手机号',
    status        VARCHAR(16)  NOT NULL DEFAULT 'NORMAL' COMMENT '账号状态：NORMAL/BANNED',
    profile_id    BIGINT NULL COMMENT '关联的用户档案ID',
    created_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    CONSTRAINT fk_sysuser_profile FOREIGN KEY (profile_id) REFERENCES user_profile(id)
) COMMENT='系统登录用户';

-- =========================
-- 模拟数据（开发/测试使用）
-- =========================

-- 用户基础信息
INSERT INTO user_profile
  (id, username, gender, age, height_cm, weight_kg, body_fat_pct,
   fitness_goal, experience_years, training_scene, allergy_foods)
VALUES
  (1, 'test_user', 1, 28, 175.00, 72.50, 18.50, 2, 1, '健身房', '花生'),
  (2, 'admin_profile', 2, 32, 162.00, 58.00, 22.00, 1, 3, '健身房+家用器械', NULL)
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- 系统账号（密码为占位示例 hash，实际登录建议通过注册生成）
INSERT INTO sys_user
  (id, username, password_hash, role, email, phone, status, profile_id)
VALUES
  (1, 'test_user',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
   'USER', NULL, NULL, 'NORMAL', 1),
  (2, 'admin_user',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
   'ADMIN', NULL, NULL, 'NORMAL', 2)
ON DUPLICATE KEY UPDATE role = VALUES(role), profile_id = VALUES(profile_id);

-- 食谱模板
INSERT INTO diet_template
  (id, fitness_type, name, total_calorie, carb_grams, protein_grams, fat_grams,
   meal_plan, cooking_tips, image_url, tags, suitable_level, estimated_cost_level)
VALUES
  (1, 1, '增肌 · 高蛋白均衡套餐',
   2600, 260.0, 180.0, 70.0,
   '早餐：全麦面包2片 + 煎鸡蛋2个 + 250ml牛奶
午餐：150g鸡胸肉 + 150g糙米饭 + 大份杂蔬
加餐：一杯乳清蛋白 + 一根香蕉
晚餐：三文鱼120g + 土豆泥100g + 西兰花',
   '增肌期优先保证蛋白质和总热量，烹饪方式以煮、炖、烤为主，减少煎炸。',
   NULL, '上班族,中餐,高蛋白', 'INTERMEDIATE', 'MEDIUM'),
  (2, 2, '减脂 · 低脂高纤维套餐',
   1800, 160.0, 110.0, 50.0,
   '早餐：燕麦粥一碗 + 水煮蛋1个 + 无糖酸奶
午餐：水煮鸡胸肉120g + 生菜沙拉 + 一小份糙米
加餐：黄瓜/小番茄
晚餐：虾仁炒西兰花 + 豆腐一小块',
   '减脂期控制总热量和脂肪，多用蒸煮焯，少油少盐，多吃蔬菜和优质蛋白。',
   NULL, '减脂,上班族,低预算', 'NEWBIE', 'LOW'),
  (3, 3, '塑形 · 均衡控制碳水套餐',
   2100, 190.0, 130.0, 60.0,
   '早餐：鸡蛋全麦三明治 + 黑咖啡
午餐：牛肉100g + 藜麦蔬菜沙拉
加餐：坚果一小把（15g）
晚餐：鸡腿肉去皮120g + 南瓜/红薯100g + 绿色蔬菜',
   '适度控制碳水比例，保证足够蛋白和训练表现，晚餐适当提前。',
   NULL, '塑形,中餐', 'INTERMEDIATE', 'MEDIUM')
ON DUPLICATE KEY UPDATE name = VALUES(name), total_calorie = VALUES(total_calorie);

-- 训练动作
INSERT INTO train_action
  (id, muscle_group, name, description, sets_suggest, reps_suggest, weight_suggest,
   image_url, video_url, difficulty, equipment, duration_minutes)
VALUES
  (1, 'chest', '杠铃卧推',
   '经典胸大肌复合动作，注意肩胛后缩、双脚稳踏地面，下放至胸中部略下位置。',
   4, 8, '1RM 的 70% 左右，中等偏重', NULL, NULL, 2, 'BARBELL', 15),
  (2, 'chest', '哑铃上斜卧推',
   '重点刺激上胸，凳子角度建议 30° 左右，避免角度过大变成肩部发力。',
   3, 10, '中等重量，保证动作幅度', NULL, NULL, 2, 'DUMBBELL', 12),
  (3, 'back', '宽握引体向上',
   '背阔肌主力动作，身体微微后倾，控制下放速度，避免借力甩动。',
   4, 6, '自身体重或辅助弹力带', NULL, NULL, 2, 'BODYWEIGHT', 10),
  (4, 'leg', '杠铃深蹲',
   '下半身综合动作，注意腰背中立，膝盖与脚尖同向，深度至少到大腿与地面平行。',
   4, 6, '1RM 的 65%–75%，根据经验调整', NULL, NULL, 3, 'BARBELL', 15),
  (5, 'core', '平板支撑',
   '保持从头到脚一条直线，避免塌腰或翘臀，过程中保持均匀呼吸。',
   3, 30, '每组 30–45 秒，自身体重', NULL, NULL, 1, 'BODYWEIGHT', 8)
ON DUPLICATE KEY UPDATE name = VALUES(name), muscle_group = VALUES(muscle_group);

-- 食谱收藏
INSERT INTO diet_collect (id, user_id, diet_id)
VALUES
  (1, 1, 2),
  (2, 2, 1)
ON DUPLICATE KEY UPDATE diet_id = VALUES(diet_id);

-- 体重记录（用于曲线）
INSERT INTO user_weight_log
  (id, user_id, log_date, weight_kg)
VALUES
  (1, 1, DATE_SUB(CURDATE(), INTERVAL 14 DAY), 74.0),
  (2, 1, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 73.0),
  (3, 1, CURDATE(), 72.5),
  (4, 2, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 59.0),
  (5, 2, CURDATE(), 58.5)
ON DUPLICATE KEY UPDATE weight_kg = VALUES(weight_kg);

-- 打卡记录
INSERT INTO check_in
  (id, user_id, check_date)
VALUES
  (1, 1, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
  (2, 1, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
  (3, 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
  (4, 1, CURDATE()),
  (5, 2, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
  (6, 2, CURDATE())
ON DUPLICATE KEY UPDATE check_date = VALUES(check_date);



-- 用户健身视频表
CREATE TABLE IF NOT EXISTS user_video (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT NOT NULL COMMENT '用户ID',
    action_id    BIGINT NOT NULL COMMENT '关联的训练动作ID',
    video_url    VARCHAR(512) NOT NULL COMMENT '视频URL',
    thumbnail_url VARCHAR(512) NULL COMMENT '封面图URL',
    category     VARCHAR(32) NULL COMMENT '视频分类/动作名称',
    duration_sec INT DEFAULT 0 COMMENT '视频时长(秒)',
    file_size    BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_uservideo_user FOREIGN KEY (user_id) REFERENCES user_profile(id),
    CONSTRAINT fk_uservideo_action FOREIGN KEY (action_id) REFERENCES train_action(id)
) COMMENT='用户健身视频';

-- 视频对比记录表
CREATE TABLE IF NOT EXISTS video_compare_record (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    user_video_id   BIGINT NOT NULL COMMENT '用户视频ID',
    std_action_id   BIGINT NOT NULL COMMENT '标准动作ID',
    compare_result  JSON NULL COMMENT '对比结果JSON',
    overall_score   INT DEFAULT 0 COMMENT '综合评分(0-100)',
    status          VARCHAR(16) DEFAULT 'COMPLETED' COMMENT '状态: PENDING/COMPLETED/FAILED',
    created_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_compare_user FOREIGN KEY (user_id) REFERENCES user_profile(id),
    CONSTRAINT fk_compare_uservideo FOREIGN KEY (user_video_id) REFERENCES user_video(id),
    CONSTRAINT fk_compare_stdaction FOREIGN KEY (std_action_id) REFERENCES train_action(id)
) COMMENT='视频对比记录';

-- 关键部位标注配置表
CREATE TABLE IF NOT EXISTS keypoint_annotation (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    action_id     BIGINT NOT NULL COMMENT '动作ID',
    keypoint_type VARCHAR(32) NOT NULL COMMENT '关键点类型: shoulder/elbow/back/knee/hip',
    x_ratio       DECIMAL(5,4) DEFAULT 0.5 COMMENT 'X坐标比例(0-1)',
    y_ratio       DECIMAL(5,4) DEFAULT 0.5 COMMENT 'Y坐标比例(0-1)',
    standard_angle INT DEFAULT 90 COMMENT '标准角度',
    angle_tolerance INT DEFAULT 15 COMMENT '角度容差',
    color         VARCHAR(16) DEFAULT 'green' COMMENT '颜色: green/yellow/red',
    description   VARCHAR(128) NULL COMMENT '描述',
    show_arc      TINYINT DEFAULT 1 COMMENT '是否显示角度弧线',
    show_range    TINYINT DEFAULT 1 COMMENT '是否显示幅度范围框',
    show_trail    TINYINT DEFAULT 0 COMMENT '是否显示轨迹引导线',
    created_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_annotation_action FOREIGN KEY (action_id) REFERENCES train_action(id),
    UNIQUE KEY uk_action_keypoint (action_id, keypoint_type)
) COMMENT='关键部位标注配置';
