package cn.pumluda.rateLimiter.aop;

import cn.pumluda.rateLimiter.annotations.AccessRateLimit;
import cn.pumluda.types.annotations.DCCValue;
import cn.pumluda.types.common.RedisConstants;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Project: group-buy-market-better <p>
 * File: RateLimiterAOP <p>
 * Created by: 16374 <p>
 * Date: 2026/5/27 <p>
 * Time: 12:53 <p>
 * Description: AOP 切面 + Redisson 实现限流
 */
@Aspect
@Slf4j
public class RateLimiterAOP {

    /* 黑名单业务键名前缀 */
    private static final String BLACKLIST_KEY_PREFIX = "rate:limit:blackList";
    /* 本地限流器缓存（1分钟） */
    private final Cache<String, RateLimiter> record = CacheBuilder.newBuilder().expireAfterWrite(
            1,
            TimeUnit.MINUTES
    ).build();
    /* Redis 缓存不同业务的限流黑名单 */
    @Resource
    private RedissonClient redissonClient;
    /* 结合动态配置轮子实现动态限流开关 */
    @DCCValue("rateLimiterSwitch:open")
    private String rateLimiterSwitch;

    /* 定义 AOP 切点 */
    @Pointcut("@annotation(cn.pumluda.rateLimiter.annotations.AccessRateLimit)")
    public void aopPoint() {
    }

    /* 使用环绕通知 */
    @Around("aopPoint() && @annotation(accessRateLimit)")
    public Object doRouter(ProceedingJoinPoint jp, AccessRateLimit accessRateLimit) throws Throwable {
        if (rateLimiterSwitch.isBlank() || rateLimiterSwitch.equals("close")) {
            return jp.proceed();
        }

        String limitKey = accessRateLimit.limitKey();
        if (StringUtils.isBlank(limitKey)) {
            throw new RuntimeException("Annotation AccessRateLimit's field can not be null!");
        }

        /* 获取限流参数 */
        MethodSignature signature = (MethodSignature) jp.getSignature();
        String[] methodParamNames = signature.getParameterNames();
        Object[] args = jp.getArgs();

        String limitArg;
        if ("all".equals(limitKey)) {
            limitArg = "all"; // 限流所有请求
        } else {
            limitArg = getLimitArg(limitKey, methodParamNames, args);
        }

        if (StringUtils.isBlank(limitArg)) {
            log.error("[接口限流] 限流异常，限流参数为空 {}", limitKey);
            return jp.proceed();
        }

        /* 黑名单拦截 */
        long remainSeconds = getBlacklistRemainSeconds(limitArg);
        if (!"all".equals(limitArg) && accessRateLimit.blockThreshold() != 0 && remainSeconds > 0) {

            long remainHours = remainSeconds / 3600;
            long remainMinutes = (remainSeconds % 3600) / 60;

            log.info(
                    "[接口限流] 黑名单拦截（剩余 {}h {}m）：{}",
                    remainHours,
                    remainMinutes,
                    limitArg
            );

            return fallbackResult(jp, accessRateLimit.fallback());
        }

        /* 调用本地限流器（Guava） */
        RateLimiter rateLimiter = record.getIfPresent(limitArg);
        if (null == rateLimiter) {
            rateLimiter = RateLimiter.create(accessRateLimit.qps());
            record.put(limitArg, rateLimiter);
        }

        /* 超 QPS 上限拦截请求，即 tryAcquire() 失败的请求 */
        if (!rateLimiter.tryAcquire()) {
            log.info("[接口限流] 当前接口已达到 QPS 使用上限，开始限流，拦截参数： {}", limitArg);

            if (accessRateLimit.blockThreshold() != 0) {
                /* 如果某个请求拦截次数过高，则加入黑名单 */
                long count = incrementInterceptCount(limitArg);
                if (count >= accessRateLimit.blockThreshold()) {
                    addBlacklist(limitArg);
                    log.info("[接口限流] {} 进入黑名单", limitArg);
                }
            }
            return fallbackResult(jp, accessRateLimit.fallback());
        }

        return jp.proceed();
    }

    /**
     * 增加拦截触发次数
     */
    private long incrementInterceptCount(String limitArg) {

        String key =
                BLACKLIST_KEY_PREFIX + ":" + RedisConstants.CREATE_ORDER + ":" + limitArg + ":" +
                "count";

        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);

        long count = atomicLong.incrementAndGet();

        // 首次设置过期时间
        if (count == 1) {
            atomicLong.expire(Duration.ofHours(24));
        }

        return count;
    }

    /**
     * 添加黑名单
     */
    private void addBlacklist(String limitArg) {

        String key = BLACKLIST_KEY_PREFIX + ":" + RedisConstants.CREATE_ORDER + ":" + limitArg;


        RBucket<String> bucket = redissonClient.getBucket(key);

        bucket.set("1", Duration.ofHours(24));
    }

    /**
     * 获取黑名单剩余封禁时间（秒）
     *
     * @return > 0 : 仍在黑名单中
     * <=0 : 不在黑名单中
     */
    private long getBlacklistRemainSeconds(String limitArg) {

        String key = BLACKLIST_KEY_PREFIX + ":" + RedisConstants.CREATE_ORDER + ":" + limitArg;

        RBucket<String> bucket = redissonClient.getBucket(key);

        return bucket.remainTimeToLive() / 1000;
    }

    /**
     * 调用拦截回调方法
     */
    private Object fallbackResult(JoinPoint jp, String fallbackMethod) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Signature signature = jp.getSignature();

        MethodSignature methodSignature = (MethodSignature) signature;

        Method method = jp.getTarget().getClass().getMethod(
                fallbackMethod,
                methodSignature.getParameterTypes()
        );

        return method.invoke(jp.getThis(), jp.getArgs());
    }

    /*
     * 依据具体参数限流
     * 1. 如果当前参数是基础类型
     * -> 判断形参名是否等于 attr
     * -> 是则返回该参数值
     *
     * 2. 如果当前参数是对象
     * -> 反射获取对象字段 attr
     * -> 获取成功则返回
     *  */
    public String getLimitArg(String limitKey, String[] methodParamName, Object[] args) {
        for (int i = 0; i < args.length; i++) {

            Object arg = args[i];
            if (arg == null) continue;

            /* 如果是基础类型参数，则判断形参名 */
            if (arg instanceof String || arg instanceof Number || arg instanceof Boolean) {
                if (methodParamName[i].equals(limitKey)) {
                    return arg.toString();
                }
                continue;
            }

            /* 如果是对象参数，则判断字段名 */
            try {
                Object argValue = getFieldValue(arg, limitKey);
                if (argValue != null) {
                    return argValue.toString();
                }
            } catch (Exception e) {
                log.error("限流异常，没有找到限流参数 {}", limitKey, e);
            }
        }
        return null;
    }

    /**
     * 反射获取对象字段值（支持父类字段）
     *
     * @param obj       目标对象
     * @param fieldName 字段名
     * @return 字段值，如果字段不存在返回 null
     */
    public Object getFieldValue(Object obj, String fieldName) {
        if (obj == null || StringUtils.isBlank(fieldName)) {
            return null;
        }

        try {
            Field field = findField(obj.getClass(), fieldName);
            if (field == null) {
                return null;
            }
            field.setAccessible(true);
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to get field value: " + fieldName, e);
        }
    }

    /**
     * 递归查找字段（支持父类和接口）
     */
    private Field findField(Class<?> clazz, String fieldName) {
        if (clazz == null || clazz == Object.class) {
            return null;
        }

        try {
            // 先查当前类
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // 当前类没有，递归查找父类
            return findField(clazz.getSuperclass(), fieldName);
        }
    }

}
