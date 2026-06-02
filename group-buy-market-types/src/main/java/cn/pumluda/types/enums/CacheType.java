package cn.pumluda.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Project: group-buy-market-better <p>
 * File: CacheType <p>
 * Created by: 16374 <p>
 * Date: 2026/6/2 <p>
 * Time: 08:44 <p>
 * Description: 不同缓存枚举
 */
@Getter
@AllArgsConstructor
public enum CacheType {

    GROUP_TEAM("GROUP_TEAM"),

    ACTIVITY_ORDER_RECORD("ACTIVITY_ORDER_RECORD"),

    SKU("SKU"),

    ACTIVITY_CONFIG("ACTIVITY_CONFIG");

    private final String name;

}