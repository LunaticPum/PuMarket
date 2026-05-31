package cn.pumluda.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Project: group-buy-market-better <p>
 * File: DiscountTypeEnum <p>
 * Created by: 16374 <p>
 * Date: 2026/5/30 <p>
 * Time: 08:43 <p>
 * Description: 活动类型枚举
 */
@Getter
@AllArgsConstructor
public enum ActivityTypeEnum {

    DEFAULT(0, "默认活动：无优惠策略"),

    GROUP_BUY(1, "拼团活动"),

    BUNDLE_DEAL(2, "凑单活动"),
    ;

    private final int code;
    private final String name;

    public static ActivityTypeEnum of(int code) {
        return switch (code) {
            case 1 -> GROUP_BUY;
            case 2 -> BUNDLE_DEAL;
            default -> DEFAULT;
        };
    }
}
