package cn.pumluda.config;

import cn.pumluda.types.annotations.DCCValue;
import cn.pumluda.types.common.SplitConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Project: group-buy-market-pumluda <p>
 * File: DCCTopicFactory <p>
 * Created by: 16374 <p>
 * Date: 2026/5/16 <p>
 * Time: 16:12 <p>
 * Description: 动态配置切换PB工厂类
 */

@Slf4j
@Configuration
public class DCCTopicFactory implements BeanPostProcessor {
    private static final String BASE_CONFIG_PATH = "group:buy:market:dcc:";
    private final RedissonClient redissonClient;
    private final Map<String, Object> dccObjGroup = new HashMap<>();

    public DCCTopicFactory(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Bean("dccTopic")
    public RTopic dccTopicListener(RedissonClient redissonClient) {
        RTopic topic = redissonClient.getTopic("group_buy_market_dcc");
        topic.addListener(String.class, (charSequence, string) -> {
            String[] splits = string.split(SplitConstants.SPLIT);

            String key = BASE_CONFIG_PATH.concat(splits[0]);
            String value = splits[1];

            RBucket<String> bucket = redissonClient.getBucket(key);
            if (!bucket.isExists()) {
                log.warn("没有此类执行配置 {}", key);
                return;
            } else {
                bucket.set(value);
            }

            Object bean = dccObjGroup.get(key);
            if (bean == null) {
                log.warn("执行配置信息丢失 {}", key);
                return;
            }

            Class<?> beanClass = bean.getClass();
            if (AopUtils.isAopProxy(bean)) {
                beanClass = AopUtils.getTargetClass(bean);
            }

            try {
                Field field = beanClass.getDeclaredField(splits[0]);
                field.setAccessible(true);
                field.set(bean, value);
                field.setAccessible(false);

                log.info("[DCC 配置切换] 执行配置 {} 以切换为 {}", splits[0], value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return topic;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetBeanClass = bean.getClass();
        Object targetBeanObject = bean;
        if (AopUtils.isAopProxy(bean)) {
            targetBeanClass = AopUtils.getTargetClass(bean);
            targetBeanObject = AopProxyUtils.getSingletonTarget(bean);
        }

        Field[] fields = targetBeanClass.getDeclaredFields();
        String setValue;
        for (Field field : fields) {
            if (!field.isAnnotationPresent(DCCValue.class)) {
                continue;
            }

            DCCValue dccValue = field.getAnnotation(DCCValue.class);
            if (StringUtils.isBlank(dccValue.value())) {
                throw new RuntimeException(field.getName() + " 未设置默认DCC配置参数");
            }

            String[] splits = dccValue.value().split(":");
            String key = BASE_CONFIG_PATH.concat(splits[0]); // 为key添加业务前缀，方便后续 Redis 缓存管理
            String defaultValue = splits[1];

            try {
                RBucket<String> bucket = redissonClient.getBucket(key);
                boolean ex = bucket.isExists();
                if (!ex) {
                    bucket.set(defaultValue);
                    setValue = defaultValue;
                } else {
                    setValue = bucket.get();
                }

                field.setAccessible(true);
                field.set(targetBeanObject, setValue);
                field.setAccessible(false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            dccObjGroup.put(key, targetBeanObject);
        }

        return bean;
    }
}
