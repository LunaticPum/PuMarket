package cn.pumluda.infrastructure.adapter.repository;

import cn.pumluda.domain.trade.adapter.repository.ITradeRepository;
import cn.pumluda.domain.trade.model.entity.*;
import cn.pumluda.infrastructure.cache.ICacheService;
import cn.pumluda.infrastructure.dao.IActivityConfigDao;
import cn.pumluda.infrastructure.dao.ISkuDao;
import cn.pumluda.infrastructure.dao.po.ActivityConfigPo;
import cn.pumluda.infrastructure.dao.po.SkuPo;
import cn.pumluda.types.common.RedisConstants;
import cn.pumluda.types.utils.RedisKeyBuilder;
import cn.pumluda.types.utils.juc.CompletableFutureUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Project: group-buy-market-better <p>
 * File: TradeRepository <p>
 * Created by: 16374 <p>
 * Date: 2026/5/28 <p>
 * Time: 09:23 <p>
 * Description: 交易业务仓储接口实现
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TradeRepository implements ITradeRepository {

    private final IActivityConfigDao activityConfigDao;
    private final ISkuDao skuDao;
    private final ICacheService cacheService;
    private final CompletableFutureUtils asyncUtil;

    @Override
    public SkuEntity getSkuById(Long skuId) {

        String key = RedisKeyBuilder.buildKey(RedisConstants.CACHE_SKU, skuId);
        SkuEntity cached = cacheService.get(key, SkuEntity.class);
        if (cached != null) {
            return cached;  // 缓存命中
        }

        SkuPo skuPo = skuDao.getSkuById(skuId);  // 缓存未命中，查 DB

        if (skuPo != null) {
            /* 如果 DB 命中，先写入缓存在返回数据给上游 */
            SkuEntity entity = new SkuEntity();
            /* BeanUtils：po -> entity */
            BeanUtils.copyProperties(skuPo, entity);

            // 默认缓存 TTL：10分钟 + 0 ~ 5 分钟的扰动
            long ttlWithSalt =
                    RedisConstants.CACHE_EXPIRE_MINUTES + ThreadLocalRandom.current().nextLong(5);
            cacheService.set(key, entity, ttlWithSalt, TimeUnit.MINUTES);

            return entity;
        } else {
            // 空对象 TTL：20秒 + 0 ~ 5 秒的扰动
            long ttlWithSalt =
                    RedisConstants.NULL_EXPIRE_SECONDS + ThreadLocalRandom.current().nextLong(5);
            cacheService.set(key, RedisConstants.NULL_PLACEHOLDER, ttlWithSalt, TimeUnit.SECONDS);

            return null;
        }
    }

    @Override
    public ActivityConfigEntity getActivityByActivityId(Long activityId) {

        String key = RedisKeyBuilder.buildKey(RedisConstants.CACHE_ACTIVITY_CONFIG, activityId);
        ActivityConfigEntity cached = cacheService.get(key, ActivityConfigEntity.class);
        if (cached != null) {
            return cached;  // 缓存命中
        }

        ActivityConfigPo activityConfigPo = activityConfigDao.getActivityByActivityId(activityId);

        if (activityConfigPo != null) {
            /* 如果 DB 命中，先写入缓存在返回数据给上游 */
            ActivityConfigEntity entity = new ActivityConfigEntity();
            BeanUtils.copyProperties(activityConfigPo, entity);

            // 默认缓存 TTL：10分钟 + 0 ~ 5 分钟的扰动
            long ttlWithSalt =
                    RedisConstants.CACHE_EXPIRE_MINUTES + ThreadLocalRandom.current().nextLong(5);
            cacheService.set(key, entity, ttlWithSalt, TimeUnit.MINUTES);

            return entity;
        } else {
            // 空对象 TTL：20秒 + 0 ~ 5 秒的扰动
            long ttlWithSalt =
                    RedisConstants.NULL_EXPIRE_SECONDS + ThreadLocalRandom.current().nextLong(5);
            cacheService.set(key, RedisConstants.NULL_PLACEHOLDER, ttlWithSalt, TimeUnit.SECONDS);

            return null;
        }
    }

    @Override
    public UserTagRecordEntity getUserTagRecordById(Long userId) {
        return null;
    }

    @Override
    public List<UserTagRecordEntity> getUserTagRecordByTag(Integer userTag) {
        return List.of();
    }

    @Override
    public GroupTeamEntity getGroupTeamByTeamId(Long groupTeamId) {
        return null;
    }

    @Override
    public GroupTeamEntity getGroupTeamByLeaderUserId(Long leaderUserId) {
        return null;
    }

    @Override
    public List<GroupTeamEntity> getGroupTeamByActivityId(Long activityId) {
        return List.of();
    }

    // todo 待实现。。


    @Override
    public ActivityOrderRecordEntity getActivityOrderRecord(String orederNo, Long activityId) {
        return null;
    }

    @Override
    public void addGroupTeam(GroupTeamEntity groupTeam) {

    }

    @Override
    public void addActivityOrderRecord(ActivityOrderRecordEntity activityOrderRecord) {

    }

    @Override
    public int reserveSkuStock(Long skuId, int quantity) {
        return 0;
    }

    /* 缓存预热：通过 BeanPostProcessor 实现，利用 @EventListener 定义监听到应用完全启动后自动执行的方法 */
    @EventListener(ApplicationReadyEvent.class)
    public void preloadHotData() {
        log.info("[仓储实现层] ========== 缓存预热开始 ==========");
        try {
            asyncUtil.runParallel(
                    () -> {
                        /* SKU 预热 */
                        List<SkuPo> allSku = skuDao.getAllSku();
                        if (allSku != null && !allSku.isEmpty()) {
                            log.info("[仓储实现层] 商品 SKU 数据预热中");
                            for (SkuPo sku : allSku) {
                                String key = RedisKeyBuilder.buildKey(
                                        RedisConstants.CACHE_SKU,
                                        sku.getSkuId()
                                );
                                SkuEntity entity = new SkuEntity();
                                BeanUtils.copyProperties(sku, entity);

                                long ttlWithSalt =
                                        RedisConstants.CACHE_EXPIRE_MINUTES +
                                        ThreadLocalRandom.current().nextLong(5);
                                cacheService.set(key, entity, ttlWithSalt, TimeUnit.MINUTES);
                            }
                            log.info("[仓储实现层] 商品 SKU 数据预热完成，数量：{}", allSku.size());
                        }
                    },
                    () -> {
                        /* ActivityConfig 预热 */
                        List<ActivityConfigPo> allActivity = activityConfigDao.getAllActivity();
                        if (allActivity != null && !allActivity.isEmpty()) {
                            log.info("[仓储实现层] 活动配置数据预热中");
                            for (ActivityConfigPo activity : allActivity) {
                                String key = RedisKeyBuilder.buildKey(
                                        RedisConstants.CACHE_ACTIVITY_CONFIG,
                                        activity.getActivityId()
                                );

                                ActivityConfigEntity entity = new ActivityConfigEntity();
                                BeanUtils.copyProperties(activity, entity);

                                long ttlWithSalt =
                                        RedisConstants.CACHE_EXPIRE_MINUTES +
                                        ThreadLocalRandom.current().nextLong(5);
                                cacheService.set(key, entity, ttlWithSalt, TimeUnit.MINUTES);
                            }
                            log.info(
                                    "[仓储实现层] 活动配置数据预热完成，数量：{}",
                                    allActivity.size()
                            );
                        }
                    }
            );
        } catch (Exception e) {
            log.warn("[仓储实现层] 缓存预热出现异常", e);
        }
        log.info("[仓储实现层] ========== 缓存预热结束 ==========");
    }

}
