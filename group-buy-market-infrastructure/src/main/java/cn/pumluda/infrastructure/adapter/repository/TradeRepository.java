package cn.pumluda.infrastructure.adapter.repository;

import cn.pumluda.domain.trade.adapter.repository.ITradeRepository;
import cn.pumluda.domain.trade.model.entity.*;
import cn.pumluda.infrastructure.cache.ICacheManager;
import cn.pumluda.infrastructure.cache.ICacheService;
import cn.pumluda.infrastructure.dao.*;
import cn.pumluda.infrastructure.dao.po.*;
import cn.pumluda.types.common.RedisConstants;
import cn.pumluda.types.enums.ActivityParticipationTypeEnum;
import cn.pumluda.types.enums.DiscountTypeEnum;
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
    private final IActivityOrderRecordDao activityOrderRecordDao;
    private final IGroupTeamDao groupTeamDao;
    private final ISkuDao skuDao;
    private final ITradeOrderDao tradeOrderDao;
    private final ITradeOrderItemDao tradeOrderItemDao;
    private final IUserTagRecordDao userTagRecordDao;

    private final ICacheService cacheService;
    private final ICacheManager cacheManager;
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
                    RedisConstants.CACHE_EXPIRE_MINUTES + ThreadLocalRandom.current().nextLong(
                            0,
                            10
                    );
            cacheService.set(key, entity, ttlWithSalt, TimeUnit.MINUTES);

            return entity;
        } else {
            // 空对象 TTL：20秒 + 0 ~ 5 秒的扰动
            long ttlWithSalt =
                    RedisConstants.NULL_EXPIRE_SECONDS + ThreadLocalRandom.current().nextLong(
                            0,
                            10
                    );
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

            /* 部分字段转换 */
            entity.setDiscountType(DiscountTypeEnum.of(activityConfigPo.getDiscountType()));

            // 默认缓存 TTL：10分钟 + 0 ~ 5 分钟的扰动
            long ttlWithSalt =
                    RedisConstants.CACHE_EXPIRE_MINUTES + ThreadLocalRandom.current().nextLong(
                            0,
                            10
                    );
            cacheService.set(key, entity, ttlWithSalt, TimeUnit.MINUTES);

            return entity;
        } else {
            // 空对象 TTL：20秒 + 0 ~ 5 秒的扰动
            long ttlWithSalt =
                    RedisConstants.NULL_EXPIRE_SECONDS + ThreadLocalRandom.current().nextLong(
                            0,
                            10
                    );
            cacheService.set(key, RedisConstants.NULL_PLACEHOLDER, ttlWithSalt, TimeUnit.SECONDS);

            return null;
        }
    }

    @Override
    public UserTagRecordEntity getUserTagRecordById(Long userId) {

        String key = RedisKeyBuilder.buildKey(RedisConstants.CACHE_USER_TAG_RECORD, userId);
        UserTagRecordEntity cached = cacheService.get(key, UserTagRecordEntity.class);
        if (cached != null) {
            return cached;  // 缓存命中
        }

        UserTagRecordPo userTagRecordPo = userTagRecordDao.getUserTagRecordById(userId);

        if (userTagRecordPo != null) {
            /* 如果 DB 命中，先写入缓存在返回数据给上游 */
            UserTagRecordEntity entity = new UserTagRecordEntity();
            BeanUtils.copyProperties(userTagRecordPo, entity);

            // 默认缓存 TTL：10分钟 + 0 ~ 5 分钟的扰动
            long ttlWithSalt =
                    RedisConstants.CACHE_EXPIRE_MINUTES + ThreadLocalRandom.current().nextLong(
                            0,
                            10
                    );
            cacheService.set(key, entity, ttlWithSalt, TimeUnit.MINUTES);

            return entity;
        } else {
            // 空对象 TTL：20秒 + 0 ~ 5 秒的扰动
            long ttlWithSalt =
                    RedisConstants.NULL_EXPIRE_SECONDS + ThreadLocalRandom.current().nextLong(
                            0,
                            10
                    );
            cacheService.set(key, RedisConstants.NULL_PLACEHOLDER, ttlWithSalt, TimeUnit.SECONDS);

            return null;
        }
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
    public ActivityOrderRecordEntity getActivityOrderRecord(String orderNo, Long activityId) {

        String key = RedisKeyBuilder.buildKey(
                RedisConstants.CACHE_ACTIVITY_ORDER_RECORD,
                orderNo,
                activityId
        );
        ActivityOrderRecordEntity cached = cacheService.get(key, ActivityOrderRecordEntity.class);
        if (cached != null) {
            return cached;  // 缓存命中
        }

        ActivityOrderRecordPo activityOrderRecord = activityOrderRecordDao.getActivityOrderRecord(
                orderNo,
                activityId
        );

        if (activityOrderRecord != null) {
            /* 如果 DB 命中，先写入缓存在返回数据给上游 */
            ActivityOrderRecordEntity entity = new ActivityOrderRecordEntity();
            BeanUtils.copyProperties(activityOrderRecord, entity);

            /* 部分字段转换 */
            entity.setParticipationType(ActivityParticipationTypeEnum.of(activityOrderRecord.getParticipationType()));

            // 默认缓存 TTL：10分钟 + -3 ~ 3 分钟的扰动
            long ttlWithSalt =
                    RedisConstants.CACHE_EXPIRE_MINUTES + ThreadLocalRandom.current().nextLong(
                            0,
                            10
                    );
            cacheService.set(key, entity, ttlWithSalt, TimeUnit.MINUTES);

            return entity;
        } else {
            // 空对象 TTL：20秒 + -3 ~ 3 秒的扰动
            long ttlWithSalt =
                    RedisConstants.NULL_EXPIRE_SECONDS + ThreadLocalRandom.current().nextLong(
                            0,
                            10
                    );
            cacheService.set(key, RedisConstants.NULL_PLACEHOLDER, ttlWithSalt, TimeUnit.SECONDS);

            return null;
        }
    }

    @Override
    public void addGroupTeam(GroupTeamEntity groupTeam) {
        GroupTeamPo groupTeamPo = new GroupTeamPo();
        BeanUtils.copyProperties(groupTeam, groupTeamPo);

        groupTeamDao.addGroupTeam(groupTeamPo);

        // todo 异步写缓存 + MQ消息兜底机制

        // todo entity 转换，不要将整个entity存入缓存，只存必要信息

//        Long activityId = groupTeam.getActivityId();
//        Long groupTeamId = groupTeam.getGroupTeamId();
//
//        String key = RedisKeyBuilder.buildKey(
//                RedisConstants.CACHE_ACTIVITY_ORDER_RECORD,
//                activityId,
//                groupTeamId
//        );
//
//        long ttlWithSalt =
//                RedisConstants.CACHE_EXPIRE_MINUTES + ThreadLocalRandom.current().nextLong(0, 10);
//
//        cacheService.set(key, groupTeam, ttlWithSalt, TimeUnit.MINUTES);

    }

    @Override
    public void addActivityOrderRecord(ActivityOrderRecordEntity activityOrderRecord) {
        ActivityOrderRecordPo activityOrderRecordPo = new ActivityOrderRecordPo();
        BeanUtils.copyProperties(activityOrderRecord, activityOrderRecordPo);
        activityOrderRecordDao.addActivityOrderRecord(activityOrderRecordPo);

        // todo 异步写缓存 + MQ消息兜底机制

        // todo entity 转换，不要将整个entity存入缓存，只存必要信息
    }

    @Override
    public void reserveSkuStock(Long skuId, int quantity) {
        skuDao.reserveSkuStock(skuId, quantity);

        // todo 异步写缓存 + MQ消息兜底机制

        // todo entity 转换，不要将整个entity存入缓存，只存必要信息
    }

    @Override
    public void addOrderItem(OrderItemEntity orderItem) {
        TradeOrderItemPo tradeOrderItemPo = new TradeOrderItemPo();
        BeanUtils.copyProperties(orderItem, tradeOrderItemPo);
        tradeOrderItemDao.addOrderItem(tradeOrderItemPo);
        // 无缓存
    }

    @Override
    public void updateTradeOrder(OrderItemEntity orderItem) {
        // todo 补充tradeOrderPo

        TradeOrderPo tradeOrderPo = new TradeOrderPo();
        tradeOrderDao.updateTradeOrder(tradeOrderPo);
        // 无缓存
    }

    /* 缓存预热：通过 BeanPostProcessor 实现，利用 @EventListener 定义监听到应用完全启动后自动执行的方法 */
    @EventListener(ApplicationReadyEvent.class)
    public void preloadHotData() {
        log.info("[仓储实现层] ========== 缓存预热开始 ==========");
        // 只预热配置项缓存：读多写少数据
        cacheManager.reloadConfidCache();
        log.info("[仓储实现层] ========== 缓存预热结束 ==========");
    }

}
