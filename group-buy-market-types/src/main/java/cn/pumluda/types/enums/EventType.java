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
public enum EventType {

    CACHE_REFRESH("CACHE_REFRESH"),

    SKU_STOCK_EXHAUSTED("SKU_STOCK_EXHAUSTED"),

    ORDER_TIMEOUT_CLOSE("ORDER_TIMEOUT_CLOSE"),

    GROUP_TIMEOUT_CLOSE("GROUP_TIMEOUT_CLOSE"),

    ACTIVITY_ORDER_RECORD_TIMEOUT_CLOSE("ACTIVITY_TIMEOUT_CLOSE"),
    ;

    private final String name;

}