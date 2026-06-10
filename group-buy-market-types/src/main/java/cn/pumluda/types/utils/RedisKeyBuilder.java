package cn.pumluda.types.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * Project: group-buy-market-better <p>
 * File: RedisKeyBuilder <p>
 * Created by: 16374 <p>
 * Date: 2026/5/28 <p>
 * Time: 16:11 <p>
 * Description: Redis key 拼接
 */

public final class RedisKeyBuilder {

    public static String buildKey(String prefix, Object... key) {
        if (key == null || key.length == 0) {
            throw new IllegalArgumentException("Redis Key cannot be null");
        }

        return prefix + ":" + StringUtils.join(
                Arrays.stream(key).map(String::valueOf).toArray(),
                ":"
        );
    }

}
