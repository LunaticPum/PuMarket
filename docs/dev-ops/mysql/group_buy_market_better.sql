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
    UNIQUE KEY (`activity_id`, `user_id`),
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
 price,
 stock,
 locked_stock,
 status,
 create_time,
 update_time)
VALUES (200001,
        'Apple iPhone 16 Pro',
        8999.00,
        100,
        0,
        1,
        NOW(),
        NOW()),

       (200002,
        'Xiaomi REDMI K80',
        2499.00,
        200,
        0,
        1,
        NOW(),
        NOW());

/*!40000 ALTER TABLE `product_sku`
    ENABLE KEYS */;
UNLOCK TABLES;

# 本地消息表
/* ------------------------------------------------------------------ */
DROP TABLE IF EXISTS `mq_outbox`;

CREATE TABLE `mq_outbox`
(
    `id`              BIGINT unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `biz_id`          VARCHAR(64)     NOT NULL COMMENT '业务幂等ID（唯一）',
    `order_no`        VARCHAR(64)     NOT NULL COMMENT '订单号',
    `msg_type`        VARCHAR(32)     NOT NULL COMMENT '消息类型：ORDER_TIMEOUT 超时关单、GROUP_SUCC 拼团队伍成团、..',
    `payload`         JSON            NOT NULL COMMENT '消息体',
    `status`          TINYINT         NOT NULL DEFAULT 0 COMMENT '消息状态：0-待发送，1-发送中，2-已发送，3-失败',
    `retry_times`     INT             NOT NULL DEFAULT 3 COMMENT '已重试次数',
    `max_retry_times` INT             NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    `next_retry_time` DATETIME                 DEFAULT NULL COMMENT '下次重试时间',
    `create_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY (`biz_id`),
    UNIQUE KEY `uk_order_msg` (`order_no`, `msg_type`),
    KEY `idx_status_time` (`status`, `create_time`),
    KEY `idx_retry_time` (`next_retry_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
