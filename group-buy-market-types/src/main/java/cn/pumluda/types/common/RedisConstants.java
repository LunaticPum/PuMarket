package cn.pumluda.types.common;

import lombok.Getter;

import java.time.Duration;

/**
 * Project: group-buy-market-better <p>
 * File: RedisKeyConstants <p>
 * Created by: 16374 <p>
 * Date: 2026/5/27 <p>
 * Time: 09:25 <p>
 * Description: Redis key
 */
@Getter
public class RedisConstants {

    /* 创建订单键 */
    public final static String CREATE_ORDER = "create:order";

    /* 业务缓存键 */
    public final static String CACHE_SKU = "cache:sku";
    public final static String CACHE_ACTIVITY_CONFIG = "cache:activityConfig";
    public final static String CACHE_MESSAGE_QUEUE = "cache:msgQueue";
    public final static String CACHE_ORDER = "cache:order";
    /* 默认缓存时间：10 分钟，视内存占用情况调整 */
    public final static long CACHE_EXPIRE_MINUTES = 10;

    /* 空值占位符 */
    public final static String NULL_PLACEHOLDER = "::NULL::";
    /* 空值缓存时间：20 秒，视内存占用情况调整 */
    public final static long NULL_EXPIRE_SECONDS = 20;


}
