package cn.pumluda.types.utils.redis;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Project: group-buy-market-better <p>
 * File: RedissonIdempotencyChecker <p>
 * Created by: 16374 <p>
 * Date: 2026/5/27 <p>
 * Time: 08:32 <p>
 * Description: 幂等性校验工具类 - Redisson 实现
 */

@Service
public class IdempotencyChecker {

    /* 黑名单业务键名前缀 */
    private static final String IDEMPOTENCY_KEY_PREFIX = "idem";
    private final RedissonClient redissonClient;

    public IdempotencyChecker(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 尝试设置幂等键，如果键不存在则设置成功 `SETNX`（代表首次请求），否则失败（代表重复请求）
     *
     * @param businessKey  业务唯一标识
     * @param idempotentId 幂等唯一标识
     * @param ttlSeconds   键存活时间（秒）
     * @return true 表示首次请求，false 表示重复请求
     */
    public boolean tryAcquire(String businessKey, String idempotentId, long ttlSeconds) {
        String key = String.format("%s:%s:%s", IDEMPOTENCY_KEY_PREFIX, businessKey, idempotentId);
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.setIfAbsent("1", Duration.ofSeconds(ttlSeconds));
    }

    /**
     * 释放幂等键（业务失败时调用，允许用户使用相同幂等 ID 进行业务重试）
     *
     * @param businessKey  业务唯一标识
     * @param idempotentId 幂等唯一标识
     */
    public void release(String businessKey, String idempotentId) {
        String key = String.format("%s:%s:%s", IDEMPOTENCY_KEY_PREFIX, businessKey, idempotentId);
        redissonClient.getBucket(key).delete();
    }
}
