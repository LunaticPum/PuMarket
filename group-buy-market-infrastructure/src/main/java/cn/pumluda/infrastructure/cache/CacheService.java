package cn.pumluda.infrastructure.cache;

import cn.pumluda.types.common.RedisConstants;
import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Project: group-buy-market-better <p>
 * File: RedisCacheService <p>
 * Created by: 16374 <p>
 * Date: 2026/5/28 <p>
 * Time: 14:13 <p>
 * Description: Redis 缓存读写实现
 */

@Slf4j
@Repository
@RequiredArgsConstructor
public class CacheService implements ICacheService {

    private final RedissonClient redissonClient;

    @Override
    public <T> T get(String key, Class<T> clazz) {
        // StringCodec.INSTANCE 的作用是指定 Redis 数据的编解码方式为 String <-> byte[]
        RBucket<String> bucket = redissonClient.getBucket(key, StringCodec.INSTANCE);


        // 缓存未命中或命中的是空对象
        String json = bucket.get();
        if (json == null || RedisConstants.NULL_PLACEHOLDER.equals(json)) {
            return null;
        }

        try {
            return JSON.parseObject(json, clazz);
        } catch (Exception e) {
            log.error(
                    "[仓储实现层] 缓存反序列化失败 key={} type={}",
                    key,
                    clazz.getSimpleName(),
                    e
            );

            return null;  // 缓存读写异常一律走查库操作，不要报异常
        }
    }

    @Override
    public <T> void set(String key, T value, long ttl, TimeUnit unit) {
        RBucket<String> bucket = redissonClient.getBucket(key, StringCodec.INSTANCE);

        if (value == null) {
            // 空对象一律存 2 秒，避免长时间占用内存
            bucket.set(
                    RedisConstants.NULL_PLACEHOLDER,
                    Duration.ofMillis(unit.toMillis(RedisConstants.NULL_EXPIRE_SECONDS))
            );
        } else if (value instanceof String) {
            // 用 Duration 转换 ttl
            bucket.set((String) value, Duration.ofMillis(unit.toMillis(ttl)));
        } else {
            try {
                String json = JSON.toJSONString(value);
                bucket.set(json, Duration.ofMillis(unit.toMillis(ttl)));
            } catch (Exception e) {
                log.error("[Infrastructure:cache] 缓存序列化失败 key={}", key, e);
            }
        }
    }

    @Override
    public void delete(String key) {
        redissonClient.getBucket(key, StringCodec.INSTANCE).delete();
    }
}
