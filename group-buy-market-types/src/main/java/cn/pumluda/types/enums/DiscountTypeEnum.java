package cn.pumluda.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Project: group-buy-market-better <p>
 * File: DiscountTypeEnum <p>
 * Created by: 16374 <p>
 * Date: 2026/5/30 <p>
 * Time: 08:43 <p>
 * Description: 活动优惠配置枚举
 */
@Getter
@AllArgsConstructor
public enum DiscountTypeEnum {

    NONE(0, "none"),

    DIRECT_MINUS(1, "directMinus"), // n元立减

    DIRECT_PRICE(2, "directPrice"), // n元秒杀

    PERCENT_OFF(3, "percentOff"),  // n折优惠

    FULL_REDUCTION(4, "fullReduction"); // 满减优惠

    private final int code;
    private final String name;

    public static DiscountTypeEnum of(int code) {
        return switch (code) {
            case 1 -> DIRECT_MINUS;
            case 2 -> DIRECT_PRICE;
            case 3 -> PERCENT_OFF;
            case 4 -> FULL_REDUCTION;
            default -> NONE;
        };
    }
}
