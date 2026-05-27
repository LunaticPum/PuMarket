package cn.pumluda.rateLimiter.annotations;

import java.lang.annotation.*;

/**
 * Project: group-buy-market-better <p>
 * File: RateLimiterAccessInterceptor <p>
 * Created by: 16374 <p>
 * Date: 2026/5/27 <p>
 * Time: 12:46 <p>
 * Description: 限流入口注解
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface AccessRateLimit {

    /* 默认拦截所有字段或实参 */
    String limitKey() default "all";

    /* 限制频次（每秒最大接受请求量） */
    double qps();

    /* 动态黑名单拦截（同一客户端超过 n 次请求后加入黑名单）：0-不限制黑名单 */
    double blockThreshold() default 0;

    /* 拦截后的执行逻辑 */
    String fallback();

}
