package cn.pumluda.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Project: group-buy-market-better <p>
 * File: UserTagEnum <p>
 * Created by: 16374 <p>
 * Date: 2026/5/29 <p>
 * Time: 09:17 <p>
 * Description: 用户标签枚举
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum UserTagEnum {

    /**
     * 通过二进制位图（Bitmap）方式存储用户标签，每个标签独占一个二进制位，通过位运算实现多标签组合存储。例如：
     * 当某一用户同时具备标签：老用户 + 薅羊毛用户，则：
     * userTag = 0010 | 0100 = 0101
     * 显然数据库只需存储一个十进制整数就可以表示多个用户标签组合
     *
     * 当后续业务逻辑需要识别薅羊毛用户时，只需：
     * if (userTag & 0100 != 0)
     */

    /* 用户生命周期标签 */
    NEW_USER(1, "新用户"),
    OLD_USER(1 << 1, "老用户"),

    /* 用户价值标签 */
    ACTIVE_USER(1 << 2, "活跃用户"),
    SILENT_USER(1 << 3, "沉默用户"),
    VIP_USER(1 << 4, "高价值用户"),

    /* 营销活动标签 */
    COUPON_PREFERRED_USER(1 << 5, "优惠券偏好用户"),
    PROMOTION_PREFERRED_USER(1 << 6, "促销活动偏好用户"),
    FLASH_SALE_USER(1 << 7, "秒杀活动用户"),

    /* 风控行为标签 */
    WOOL_USER(1 << 8, "薅羊毛用户"),
    HIGH_FREQUENCY_USER(1 << 9, "高频请求用户"),
    RISK_USER(1 << 10, "风险用户"),

    /* 渠道来源标签 */
    INVITE_USER(1 << 11, "邀请用户"),
    CHANNEL_USER(1 << 12, "渠道用户");

    private int bitmap;
    private String info;

}
