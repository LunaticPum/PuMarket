/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS = @@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION = @@COLLATION_CONNECTION */;
SET NAMES utf8mb4;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO', SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES = @@SQL_NOTES, SQL_NOTES = 0 */;

CREATE database if NOT EXISTS `group_buy_market_better` default character set utf8mb4 collate utf8mb4_0900_ai_ci;
use `group_buy_market_better`;


# 订单主表
/* ------------------------------------------------------------------ */

DROP TABLE IF EXISTS `trade_order`;

CREATE TABLE `trade_order`
(
    `id`                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `order_no`          VARCHAR(64)     NOT NULL COMMENT '订单号',
    `user_id`           BIGINT          NOT NULL COMMENT '用户 ID',
    `user_tag`          INT                      DEFAULT NULL COMMENT '人群标签',
    `order_status`      TINYINT         NOT NULL DEFAULT 0 COMMENT '订单状态：0-订单创建，1-订单结算，2-订单取消，3-。。',
    `total_amount`      DECIMAL(10, 2)  NOT NULL COMMENT '原始总金额',
    `pay_amount`        DECIMAL(10, 2)  NOT NULL COMMENT '实际支付金额',
    `discount_amount`   DECIMAL(10, 2)  NOT NULL COMMENT '优惠金额',
    `entry_source`      TINYINT         NOT NULL COMMENT '流量入口（枚举类型）',
    `pay_channel`       VARCHAR(32)              DEFAULT NULL COMMENT '支付渠道',
    `order_create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `order_expire_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '有效截止时间',
    `order_end_time`    DATETIME                 DEFAULT NULL COMMENT '订单结束时间（支付/取消/异常）',
    `create_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),                                       # 按用户 ID 查询订单
    KEY `idx_order_status_expire` (`order_status`, `order_expire_time`), # 超时关单
    KEY `idx_trade_sc` (`entry_source`, `pay_channel`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

# 订单明细表
/* ------------------------------------------------------------------ */
DROP TABLE IF EXISTS `trade_order_item`;

CREATE TABLE `trade_order_item`
(
    `id`                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `order_no`             VARCHAR(64)     NOT NULL COMMENT '订单号',
    `sku_id`               BIGINT          NOT NULL COMMENT '商品 ID',
    `product_name`         VARCHAR(128) DEFAULT NULL COMMENT '商品名称',
    `origin_price`         DECIMAL(10, 2)  NOT NULL COMMENT '商品原单价',
    `quantity`             INT UNSIGNED    NOT NULL COMMENT '购买数量',
    `actual_price`         DECIMAL(10, 2)  NOT NULL COMMENT '实际成交价',
    `discount_price`       DECIMAL(10, 2)  NOT NULL COMMENT '总优惠金额',
    `activity_id`          BIGINT          NOT NULL COMMENT '活动 ID',
    `activity_type`        TINYINT         NOT NULL COMMENT '活动类型快照',
    `activity_business_id` BIGINT       DEFAULT NULL COMMENT '活动业务 ID：如果是拼团活动则为拼团队伍ID',
    PRIMARY KEY (`id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_activity_business_id` (`activity_business_id`),
    KEY `idx_order_no_sku_id` (`order_no`, `sku_id`),
    KEY idx_activity_biz (`activity_type`, `activity_business_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

# 参与活动订单记录表
/* ------------------------------------------------------------------ */
DROP TABLE IF EXISTS `activity_order_record`;

CREATE TABLE `activity_order_record`
(
    `id`                   BIGINT unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `order_no`             VARCHAR(64)     NOT NULL COMMENT '订单号',
    `user_id`              BIGINT          NOT NULL COMMENT '用户 ID',
    `activity_id`          BIGINT          NOT NULL COMMENT '活动 ID',
    `activity_name`        VARCHAR(128)             DEFAULT NULL COMMENT '活动名称',
    `activity_business_id` BIGINT          NOT NULL COMMENT '通用活动业务字段：例如，如果是拼团活动则存储拼团队伍 ID',
    `participation_type`   TINYINT         NOT NULL COMMENT '活动参与动作类型: 1-开团（团长），2-参团（团员），3-排队，4-抽签..',
    `quota_occupied`       TINYINT                  DEFAULT 0 COMMENT '是否已占用优惠名额：0-未占用，1-已占用',
    `record_status`        TINYINT         NOT NULL COMMENT '记录处理状态：0-处理中，1-成功，2-失败，3-已取消',
    `join_time`            DATETIME        NOT NULL COMMENT '参与活动时间',
    `expire_time`          DATETIME                 DEFAULT NULL COMMENT '记录定时清理时间',
    `create_time`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_order_activity` (`order_no`, `activity_id`),
    KEY `idx_user_id` (`user_id`),                                       # 查询当前用户的拼团记录
    KEY `idx_activity_id` (`activity_id`),                               # 查询活动报表
    KEY `idx_activity_business_id` (`activity_business_id`, `join_time`) # 查询某个团的所有成员，按参团时间顺序排序
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

# 拼团队伍表
/* ------------------------------------------------------------------ */
DROP TABLE IF EXISTS `group_team`;

CREATE TABLE `group_team`
(
    `id`                BIGINT unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `activity_id`       BIGINT          NOT NULL COMMENT '活动 ID',
    `group_team_id`     BIGINT          NOT NULL COMMENT '拼团队伍 ID',
    `leader_user_id`    BIGINT          NOT NULL COMMENT '团长用户 ID',
    `sku_id`            BIGINT          NOT NULL COMMENT '商品 ID',
    `required_num`      INT             NOT NULL COMMENT '成团所需人数',
    `current_num`       INT                      DEFAULT 0 COMMENT '当前参团人数',
    `settled_trade_num` INT                      DEFAULT 0 COMMENT '团内结算交易量',
    `team_status`       TINYINT                  DEFAULT 0 COMMENT '队伍状态：0-进行中，1-已成团，2-已过期，3-已取消',
    `team_create_time`  DATETIME        NOT NULL COMMENT '开团时间',
    `team_expire_time`  DATETIME        NOT NULL COMMENT '队伍有效截止时间',
    `team_end_time`     DATETIME                 DEFAULT NULL COMMENT '队伍结束时间',
    `create_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_team_id` (`group_team_id`),
    KEY `idx_activity_status` (`activity_id`, `team_status`),
    KEY `idx_user` (`leader_user_id`),
    KEY `idx_expire_time` (`team_expire_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

# 活动配置表
/* ------------------------------------------------------------------ */
DROP TABLE IF EXISTS `activity_config`;

CREATE TABLE `activity_config`
(
    `id`                   BIGINT unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `activity_id`          BIGINT          NOT NULL COMMENT '活动 ID',
    `activity_name`        VARCHAR(128)    NOT NULL COMMENT '活动名称',
    `activity_type`        TINYINT         NOT NULL COMMENT '活动类型：0-默认活动，1-拼团，2-凑单，...',
    `discount_type`        TINYINT         NOT NULL COMMENT '优惠类型：1-直减，2-直降，3-折扣，4-满减',
    `discount_config`      JSON                     DEFAULT NULL COMMENT '优惠配置',
    `total_discount_quota` INT             NOT NULL COMMENT '优惠名额总数（如果是拼团活动则该值等于成团所需人数）',
    `limit_tag`            INT             NOT NULL DEFAULT 0 COMMENT '限流标签，可输入多个人群标签，被限流的无法参加活动',
    `status`               TINYINT         NOT NULL COMMENT '活动状态：0-禁用，1-启用',
    `start_time`           DATETIME        NOT NULL COMMENT '活动开始时间',
    `end_time`             DATETIME        NOT NULL COMMENT '活动结束时间',
    `create_time`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_activity_id` (`activity_id`),
    KEY `idx_time_range` (`status`, `start_time`, `end_time`) # 查询活动有效时间
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

LOCK TABLES `activity_config` WRITE;
/*!40000 ALTER TABLE `activity_config`
    DISABLE KEYS */;

INSERT INTO `activity_config` (activity_id,
                               activity_name,
                               activity_type,
                               discount_type,
                               discount_config,
                               total_discount_quota,
                               limit_tag,
                               status,
                               start_time,
                               end_time,
                               create_time,
                               update_time)
VALUES
    -- 默认活动
    (0,
     '默认活动',
     0,
     0,
     NULL,
     0,
     0,
     1,
     '1970-01-01 00:00:00',
     '2999-12-31 23:59:59',
     NOW(),
     NOW()),

    -- 三人成团，8折优惠
    (100001,
     '新人三人拼团8折活动',
     1,
     3,
     JSON_OBJECT(
             'percent', 80
     ),
     3,
     0,
     1,
     NOW(),
     DATE_ADD(NOW(), INTERVAL 7 DAY),
     NOW(),
     NOW()),

    -- 五人成团，立减30元
    (100002,
     '五人成团立减30元',
     1,
     1,
     JSON_OBJECT(
             'amount', 30
     ),
     5,
     6,
     1,
     NOW(),
     DATE_ADD(NOW(), INTERVAL 7 DAY),
     NOW(),
     NOW()),

    -- 三人成团，直降到99元
    (100003,
     '三人成团99元秒杀',
     1,
     2,
     JSON_OBJECT(
             'price', 99
     ),
     3,
     0,
     1,
     NOW(),
     DATE_ADD(NOW(), INTERVAL 3 DAY),
     NOW(),
     NOW()),

    -- 十人成团，5折优惠
    (100004,
     '十人成团半价购',
     1,
     3,
     JSON_OBJECT(
             'percent', 50
     ),
     10,
     0,
     1,
     NOW(),
     DATE_ADD(NOW(), INTERVAL 15 DAY),
     NOW(),
     NOW()),

    -- 五人成团，满200减50
    (100005,
     '五人成团满200减50',
     1,
     4,
     JSON_OBJECT(
             'threshold', 200,
             'amount', 50
     ),
     5,
     0,
     1,
     NOW(),
     DATE_ADD(NOW(), INTERVAL 10 DAY),
     NOW(),
     NOW()),

    -- 八人成团，立减100元
    (100006,
     '八人成团立减100元',
     1,
     1,
     JSON_OBJECT(
             'amount', 100
     ),
     8,
     2,
     1,
     NOW(),
     DATE_ADD(NOW(), INTERVAL 30 DAY),
     NOW(),
     NOW());

/*!40000 ALTER TABLE `activity_config`
    ENABLE KEYS */;
UNLOCK TABLES;


# 用户标签
/* ------------------------------------------------------------------ */
/* 使用位图标签：用户标签 = 位图组合*/
DROP TABLE IF EXISTS `user_tag_record`;

CREATE TABLE `user_tag_record`
(
    `id`          BIGINT unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '用户 ID',
    `user_tag`    INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '用户标签位图',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY (`user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

# 商品表
/* ------------------------------------------------------------------ */
DROP TABLE IF EXISTS `product_sku`;

CREATE TABLE `product_sku`
(
    `sku_id`       BIGINT UNSIGNED NOT NULL COMMENT '商品 SKU ID',
    `product_name` VARCHAR(128)    NOT NULL COMMENT '商品名称',
    `image_url`    VARCHAR(512)             DEFAULT NULL COMMENT '商品主图URL',
    `description`  TEXT                     DEFAULT NULL COMMENT '商品描述',
    `price`        DECIMAL(10, 2)  NOT NULL COMMENT '商品销售价格',
    `stock`        INT             NOT NULL COMMENT '总库存数量',
    `locked_stock` INT             NOT NULL COMMENT '锁定库存数量', # 库存预扣减
    `status`       TINYINT         NOT NULL DEFAULT 1 COMMENT '商品状态：0-下架，1-上架',
    `create_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`sku_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

LOCK TABLES `product_sku` WRITE;
/*!40000 ALTER TABLE `product_sku`
    DISABLE KEYS */;

INSERT INTO `product_sku`
(sku_id,
 product_name,
 image_url,
 description,
 price,
 stock,
 locked_stock,
 status,
 create_time,
 update_time)
VALUES (200001,
        'Apple iPhone 16 Pro 256GB 沙漠金',
        'https://picsum.photos/seed/iphone16pro/400/400',
        '全新 A18 Pro 芯片，钛金属设计，4800 万像素融合式摄像头，支持 Apple Intelligence。6.3 英寸超视网膜 XDR 显示屏，USB-C 接口。',
        8999.00,
        100,
        0,
        1,
        NOW(),
        NOW()),

       (200002,
        '小米 REDMI K80 至尊版 16GB+512GB',
        'https://picsum.photos/seed/redmik80/400/400',
        '天玑 9300+ 旗舰芯片，1.5K 超窄边框直屏，120W 超级快充 + 5500mAh 大电池。光影猎人 800 主摄，IP68 防尘防水。',
        2499.00,
        200,
        0,
        1,
        NOW(),
        NOW()),

       (200003,
        '华为 Mate 70 Pro 12GB+512GB 昆仑玻璃版',
        'https://picsum.photos/seed/mate70pro/400/400',
        '麒麟 9100 芯片，HarmonyOS NEXT，卫星通信。6.9 英寸 OLED 等深微曲屏，XMAGE 影像，5500mAh 硅负极电池。',
        6999.00,
        150,
        0,
        1,
        NOW(),
        NOW()),

       (200004,
        'Sony WH-1000XM5 头戴式无线降噪耳机 铂金银',
        'https://picsum.photos/seed/sonyxm5/400/400',
        '业界领先降噪，30 小时续航，支持 LDAC 高解析度音频。8 麦克风系统，自适应声音控制，佩戴感应，快充 3 分钟播放 3 小时。',
        2299.00,
        300,
        0,
        1,
        NOW(),
        NOW()),

       (200005,
        '戴森 Dyson V15 Detect 无绳吸尘器',
        'https://picsum.photos/seed/dysonv15/400/400',
        '激光探测微尘，压电式声学传感器，LCD 屏实时显示吸入颗粒物大小和数量。240AW 强劲吸力，60 分钟续航，整机 HEPA 过滤。',
        4990.00,
        80,
        0,
        1,
        NOW(),
        NOW()),

       (200006,
        '戴森 Airwrap Complete 多功能美发器 普鲁士蓝',
        'https://picsum.photos/seed/airwrap/400/400',
        '康达效应气流科技，全新防飞翘干发风嘴，6 款风嘴配件。智能温控，多方向气流，造型同时抚平毛躁。',
        3699.00,
        60,
        0,
        1,
        NOW(),
        NOW()),

       (200007,
        'Nintendo Switch OLED 马力欧红蓝配色',
        'https://picsum.photos/seed/switcholed/400/400',
        '7 英寸 OLED 显示屏，64GB 机身存储，有线 LAN 接口。支持 TV/桌上/手提三种模式，畅玩马力欧/塞尔达/宝可梦等大作。',
        2599.00,
        120,
        0,
        1,
        NOW(),
        NOW()),

       (200008,
        'Apple AirPods Pro 第二代 (USB-C)',
        'https://picsum.photos/seed/airpodspro2/400/400',
        'H2 芯片，自适应降噪，自适应通透模式，个性化空间音频。USB-C 充电盒支持精确查找，最长 6 小时续航。',
        1799.00,
        250,
        0,
        1,
        NOW(),
        NOW()),

       (200009,
        'SK-II 神仙水 护肤精华露 230ml',
        'https://picsum.photos/seed/sk2essence/400/400',
        '超过 90% PITERA™ 经典成分，改善肌肤五大维度。清爽水状质地，快速渗透，调节肌肤水油平衡，令肌肤晶莹剔透。',
        1590.00,
        180,
        0,
        1,
        NOW(),
        NOW()),

       (200010,
        '飞利浦 Sonicare 钻石亮白智能电动牙刷 HX9352',
        'https://picsum.photos/seed/philipstooth/400/400',
        '31000 次/分钟高频震动，5 种清洁模式，Smartimer 智能计时器。玻璃充电杯 + USB 旅行充电盒，一次充电续航 3 周。',
        899.00,
        400,
        0,
        1,
        NOW(),
        NOW());

/*!40000 ALTER TABLE `product_sku`
    ENABLE KEYS */;
UNLOCK TABLES;

# 补偿任务表
/* ------------------------------------------------------------------ */
DROP TABLE IF EXISTS `mq_task`;

CREATE TABLE `mq_task`
(
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `biz_id`          VARCHAR(64)     NOT NULL COMMENT '业务幂等ID（全局唯一，用于防重复投递）',
    `biz_type`        VARCHAR(32)     NOT NULL COMMENT '业务类型：ORDER / GROUP_TEAM / SKU / USER_TAG / CACHE_REFRESH',
    `event_type`      VARCHAR(64)     NOT NULL COMMENT '事件类型：ORDER_TIMEOUT / GROUP_CREATE / SKU_UPDATE / CACHE_REFRESH',
    `payload`         JSON            NOT NULL COMMENT '消息体（事件内容）',
    `status`          TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：0-待投递 1-投递中 2-成功 3-失败',
    `retry_times`     INT             NOT NULL DEFAULT 0 COMMENT '已重试次数',
    `max_retry_times` INT             NOT NULL DEFAULT 5 COMMENT '最大重试次数',
    `next_retry_time` DATETIME                 DEFAULT NULL COMMENT '下次重试时间',
    `shard_key`       VARCHAR(64)              DEFAULT NULL COMMENT '分区Key（Kafka key，用于顺序控制）',
    `topic`           VARCHAR(128)    NOT NULL COMMENT '目标MQ topic',
    `create_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_biz_id` (biz_id),
    KEY `idx_status_retry` (`status`, `next_retry_time`),
    KEY `idx_biz_type` (`biz_type`),
    KEY `idx_topic` (`topic`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

# 营销后台用户表
/* ------------------------------------------------------------------ */
DROP TABLE IF EXISTS `marketing_user`;

CREATE TABLE `marketing_user`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `username`      VARCHAR(64)     NOT NULL COMMENT '用户名',
    `password_hash` VARCHAR(256)    NOT NULL COMMENT 'BCrypt 密码哈希',
    `role`          VARCHAR(32)     NOT NULL DEFAULT 'ADMIN' COMMENT '角色：ADMIN / OPERATOR',
    `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

LOCK TABLES `marketing_user` WRITE;
/*!40000 ALTER TABLE `marketing_user`
    DISABLE KEYS */;

-- 默认管理员账号：admin / admin123
INSERT INTO `marketing_user` (username, password_hash, role, create_time, update_time)
VALUES ('admin',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'ADMIN',
        NOW(),
        NOW());

/*!40000 ALTER TABLE `marketing_user`
    ENABLE KEYS */;
UNLOCK TABLES;
