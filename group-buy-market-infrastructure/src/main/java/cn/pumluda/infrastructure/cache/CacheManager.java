package cn.pumluda.infrastructure.cache;

import cn.pumluda.domain.trade.model.entity.*;
import cn.pumluda.infrastructure.dao.*;
import cn.pumluda.infrastructure.dao.po.ActivityConfigPo;
import cn.pumluda.infrastructure.dao.po.SkuPo;
import cn.pumluda.infrastructure.dao.po.UserTagRecordPo;
import cn.pumluda.types.common.RedisConstants;
import cn.pumluda.types.enums.DiscountTypeEnum;
import cn.pumluda.types.utils.RedisKeyBuilder;
import cn.pumluda.types.utils.juc.CompletableFutureUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Project: group-buy-market-better <p>
 * File: CacheReloadService <p>
 * Created by: 16374 <p>
 * Date: 2026/5/30 <p>
 * Time: 16:46 <p>
 * Description: 缓存重加载实现
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheManager implements ICacheManager {

    private final IActivityConfigDao activityConfigDao;
    private final IActivityOrderRecordDao activityOrderRecordDao;
    private final IGroupTeamDao groupTeamDao;
    private final ISkuDao skuDao;
    private final IUserTagRecordDao userTagRecordDao;

    private final ICacheService cacheService;
    private final CompletableFutureUtils asyncUtil;

    @Override
    public void reloadConfidCache() {
        asyncUtil.runParallel(
                this::reloadSkuCache,
                this::reloadActivityCache,
                this::reloadUserTagCache
        );
    }

    @Override
    public void reloadActivityCache() {
        try {
            log.info("[仓储实现层] 活动配置缓存重加载中");

            List<ActivityConfigPo> allActivity = activityConfigDao.getAllActivity();
            if (allActivity != null && !allActivity.isEmpty()) {
                for (ActivityConfigPo activity : allActivity) {
                    String key = RedisKeyBuilder.buildKey(
                            RedisConstants.CACHE_ACTIVITY_CONFIG,
                            activity.getActivityId()
                    );

                    ActivityConfigEntity entity = new ActivityConfigEntity();
                    BeanUtils.copyProperties(activity, entity);

                    /* 部分字段转换 */
                    entity.setDiscountType(DiscountTypeEnum.of(activity.getDiscountType()));

                    long ttlWithSalt = RedisConstants.CACHE_EXPIRE_MINUTES +
                                       ThreadLocalRandom.current().nextLong(0, 10);
                    cacheService.set(key, entity, ttlWithSalt, TimeUnit.MINUTES);
                }
                log.info("[仓储实现层] 活动配置缓存重加载完成，数量：{}", allActivity.size());
            } else {
                log.info(
                        "[仓储实现层] 活动配置记录为空"
                );
            }

        } catch (BeansException e) {
            log.error("[仓储实现层] 活动配置缓存重加载失败");
        }
    }

    @Override
    public void reloadSkuCache() {
        try {
            log.info("[仓储实现层] 商品 SKU 缓存重加载中");

            List<SkuPo> allSku = skuDao.getAllSku();
            if (allSku != null && !allSku.isEmpty()) {
                for (SkuPo sku : allSku) {
                    String key = RedisKeyBuilder.buildKey(RedisConstants.CACHE_SKU, sku.getSkuId());
                    SkuEntity entity = new SkuEntity();
                    BeanUtils.copyProperties(sku, entity);

                    long ttlWithSalt = RedisConstants.CACHE_EXPIRE_MINUTES +
                                       ThreadLocalRandom.current().nextLong(0, 10);
                    cacheService.set(key, entity, ttlWithSalt, TimeUnit.MINUTES);
                }
                log.info("[仓储实现层] 商品 SKU 缓存重加载完成，数量：{}", allSku.size());
            } else {
                log.info(
                        "[仓储实现层] 商品 SKU 记录为空"
                );
            }

        } catch (BeansException e) {
            log.error("[仓储实现层] 商品 SKU 缓存重加载失败");
        }
    }

    @Override
    public void reloadUserTagCache() {
        try {
            log.info("[仓储实现层] 用户标签记录缓存重加载中");

            List<UserTagRecordPo> allUserTagRecord = userTagRecordDao.getAllUserTagRecord();
            if (allUserTagRecord != null && !allUserTagRecord.isEmpty()) {
                for (UserTagRecordPo userTagRecord : allUserTagRecord) {
                    String key = RedisKeyBuilder.buildKey(
                            RedisConstants.CACHE_USER_TAG_RECORD,
                            userTagRecord.getUserId()
                    );
                    UserTagRecordEntity entity = new UserTagRecordEntity();
                    BeanUtils.copyProperties(userTagRecord, entity);

                    long ttlWithSalt = RedisConstants.CACHE_EXPIRE_MINUTES +
                                       ThreadLocalRandom.current().nextLong(0, 10);
                    cacheService.set(key, entity, ttlWithSalt, TimeUnit.MINUTES);
                }
                log.info(
                        "[仓储实现层] 用户标签记录缓存重加载完成，数量：{}",
                        allUserTagRecord.size()
                );
            } else {
                log.info(
                        "[仓储实现层] 用户标签记录为空"
                );
            }

        } catch (BeansException e) {
            log.error("[仓储实现层] 用户标签记录缓存重加载失败");
        }
    }

    @Override
    public void refreshGroupTeam(GroupTeamEntity groupTeam) {

    }

    @Override
    public void refreshActivityOrderRecord(ActivityOrderRecordEntity activityOrderRecord) {

    }

    @Override
    public void evictGroupTeam(String cacheKey) {

    }

    @Override
    public void evictActivityOrderRecord(String cacheKey) {

    }
}
