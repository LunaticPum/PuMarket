package cn.pumluda.infrastructure.adapter.repository;

import cn.pumluda.domain.trade.adapter.repository.ITradeRepository;
import cn.pumluda.domain.trade.model.aggregate.OrderAggregate;
import cn.pumluda.domain.trade.model.entity.*;
import cn.pumluda.domain.trade.model.valobj.GroupTeamStatusEnumVo;
import cn.pumluda.domain.trade.model.valobj.OrderStatusEnumVo;
import cn.pumluda.domain.trade.model.valobj.TradeSCVo;
import cn.pumluda.infrastructure.cache.ICacheManager;
import cn.pumluda.infrastructure.cache.ICacheService;
import cn.pumluda.infrastructure.dao.*;
import cn.pumluda.infrastructure.dao.po.*;
import cn.pumluda.types.common.RedisConstants;
import cn.pumluda.types.enums.ActivityParticipationTypeEnum;
import cn.pumluda.types.enums.DiscountTypeEnum;
import cn.pumluda.types.enums.EventType;
import cn.pumluda.types.event.EventEnvelope;
import cn.pumluda.types.utils.RedisKeyBuilder;
import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    private final IMqTaskDao mqTaskDao;

    private final ICacheService cacheService;
    private final ICacheManager cacheManager;

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

            if (skuPo.getStock() != null && skuPo.getStock() <= 0) {
                if (skuPo.getStatus() != 0) {
                    skuDao.updateSkuStatus(skuId, 0);
                }

                // 发出库存告罄通知，异步调用库存补货 RPC 接口
                EventEnvelope event = EventEnvelope.builder()
                                                   .eventType(EventType.SKU_STOCK_EXHAUSTED)
                                                   .bizId(UUID.randomUUID().toString())
                                                   .shardKey(String.valueOf(skuId))
                                                   .payload(Map.of("skuId", skuId))
                                                   .build();

                createMqTask("SKU", "restock-topic", event);
            }


            // 默认缓存 TTL：10分钟 + 0 ~ 5 分钟的扰动
            long ttlWithSalt = RedisConstants.CACHE_EXPIRE_MINUTES + ThreadLocalRandom.current().nextLong(0, 10);
            cacheService.set(key, entity, ttlWithSalt, TimeUnit.MINUTES);

            return entity;
        } else {
            // 空对象 TTL：20秒 + 0 ~ 5 秒的扰动
            long ttlWithSalt = RedisConstants.NULL_EXPIRE_SECONDS + ThreadLocalRandom.current().nextLong(0, 10);
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
            long ttlWithSalt = RedisConstants.CACHE_EXPIRE_MINUTES + ThreadLocalRandom.current().nextLong(0, 10);
            cacheService.set(key, entity, ttlWithSalt, TimeUnit.MINUTES);

            return entity;
        } else {
            // 空对象 TTL：20秒 + 0 ~ 5 秒的扰动
            long ttlWithSalt = RedisConstants.NULL_EXPIRE_SECONDS + ThreadLocalRandom.current().nextLong(0, 10);
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
            long ttlWithSalt = RedisConstants.CACHE_EXPIRE_MINUTES + ThreadLocalRandom.current().nextLong(0, 10);
            cacheService.set(key, entity, ttlWithSalt, TimeUnit.MINUTES);

            return entity;
        } else {
            // 空对象 TTL：20秒 + 0 ~ 5 秒的扰动
            long ttlWithSalt = RedisConstants.NULL_EXPIRE_SECONDS + ThreadLocalRandom.current().nextLong(0, 10);
            cacheService.set(key, RedisConstants.NULL_PLACEHOLDER, ttlWithSalt, TimeUnit.SECONDS);

            return null;
        }
    }

    @Override
    public List<UserTagRecordEntity> getUserTagRecordByTag(Integer userTag) {
        return List.of();
    }

    @Override
    public GroupTeamEntity getGroupTeam(Long activityId, Long groupTeamId) {

        String key = RedisKeyBuilder.buildKey(RedisConstants.CACHE_GROUP_TEAM, activityId, groupTeamId);
        GroupTeamEntity cached = cacheService.get(key, GroupTeamEntity.class);
        if (cached != null) {
            return cached;  // 缓存命中
        }

        GroupTeamPo groupTeamPo = groupTeamDao.getGroupTeam(activityId, groupTeamId);

        if (groupTeamPo != null) {
            /* 如果 DB 命中，先写入缓存在返回数据给上游 */
            GroupTeamEntity entity = new GroupTeamEntity();
            BeanUtils.copyProperties(groupTeamPo, entity);

            entity.setTeamStatus(GroupTeamStatusEnumVo.of(groupTeamPo.getTeamStatus()));

            // 默认缓存 TTL：10分钟 + 0 ~ 5 分钟的扰动
            long ttlWithSalt = RedisConstants.CACHE_EXPIRE_MINUTES + ThreadLocalRandom.current().nextLong(0, 10);
            cacheService.set(key, entity, ttlWithSalt, TimeUnit.MINUTES);

            return entity;
        } else {
            // 空对象 TTL：20秒 + 0 ~ 5 秒的扰动
            long ttlWithSalt = RedisConstants.NULL_EXPIRE_SECONDS + ThreadLocalRandom.current().nextLong(0, 10);
            cacheService.set(key, RedisConstants.NULL_PLACEHOLDER, ttlWithSalt, TimeUnit.SECONDS);

            return null;
        }
    }

    @Override
    public GroupTeamEntity getGroupTeamByLeaderUserId(Long leaderUserId) {
        return null;
    }

    @Override
    public List<GroupTeamEntity> getGroupTeamByActivityId(Long activityId) {
        return List.of();
    }

    // todo 额外功能，有需要再实现


    @Override
    public ActivityOrderRecordEntity getActivityOrderRecord(Long userId, Long activityId) {

        String key = RedisKeyBuilder.buildKey(RedisConstants.CACHE_ACTIVITY_ORDER_RECORD, userId, activityId);
        ActivityOrderRecordEntity cached = cacheService.get(key, ActivityOrderRecordEntity.class);
        if (cached != null) {
            return cached;  // 缓存命中
        }

        ActivityOrderRecordPo activityOrderRecord = activityOrderRecordDao.getActivityOrderRecord(userId, activityId);

        if (activityOrderRecord != null) {
            /* 如果 DB 命中，先写入缓存在返回数据给上游 */
            ActivityOrderRecordEntity entity = new ActivityOrderRecordEntity();
            BeanUtils.copyProperties(activityOrderRecord, entity);

            /* 部分字段转换 */
            entity.setParticipationType(ActivityParticipationTypeEnum.of(activityOrderRecord.getParticipationType()));

            // 默认缓存 TTL：10分钟 + -3 ~ 3 分钟的扰动
            long ttlWithSalt = RedisConstants.CACHE_EXPIRE_MINUTES + ThreadLocalRandom.current().nextLong(0, 10);
            cacheService.set(key, entity, ttlWithSalt, TimeUnit.MINUTES);

            return entity;
        } else {
            // 空对象 TTL：20秒 + -3 ~ 3 秒的扰动
            long ttlWithSalt = RedisConstants.NULL_EXPIRE_SECONDS + ThreadLocalRandom.current().nextLong(0, 10);
            cacheService.set(key, RedisConstants.NULL_PLACEHOLDER, ttlWithSalt, TimeUnit.SECONDS);

            return null;
        }
    }

    @Override
    public OrderAggregate getOrderByOrderNo(String orderNo) {

        TradeOrderPo tradeOrderPo = tradeOrderDao.getOrderByOrderNo(orderNo);
        if (null == tradeOrderPo) return null;

        OrderAggregate entity = new OrderAggregate();
        BeanUtils.copyProperties(tradeOrderPo, entity);

        UserTagRecordEntity userTagRecord = new UserTagRecordEntity(
                tradeOrderPo.getUserId(),
                                                                    tradeOrderPo.getUserTag()
        );
        TradeSCVo tradeSC = new TradeSCVo(tradeOrderPo.getEntrySource(), tradeOrderPo.getPayChannel());

        entity.setUserTagRecord(userTagRecord);
        entity.setOrderStatus(OrderStatusEnumVo.of(tradeOrderPo.getOrderStatus()));
        entity.setTradeSC(tradeSC);

        return entity;
    }

    @Override
    public void addGroupTeam(GroupTeamEntity groupTeam) {
        GroupTeamPo groupTeamPo = GroupTeamPo.builder()
                                             .activityId(groupTeam.getActivityId())
                                             .groupTeamId(groupTeam.getGroupTeamId())
                                             .leaderUserId(groupTeam.getLeaderUserId())
                                             .skuId(groupTeam.getSkuId())
                                             .requiredNum(groupTeam.getRequiredNum())
                                             .currentNum(groupTeam.getCurrentNum())
                                             .settledTradeNum(groupTeam.getSettledTradeNum())
                                             .teamStatus(groupTeam.getTeamStatus().getCode())
                                             .teamCreateTime(groupTeam.getTeamCreateTime())
                                             .teamExpireTime(groupTeam.getTeamExpireTime())
                                             .build();

        groupTeamDao.addGroupTeam(groupTeamPo);

        // todo 发送延迟消息到 Redis 消息队列，实现记录过期处理
        EventEnvelope event = EventEnvelope.builder()
                                           .eventType(EventType.CACHE_REFRESH)
                                           .bizId(UUID.randomUUID()
                                                      .toString())
                                           .shardKey(groupTeam.getActivityId().toString())
                                           .payload(Map.of(
                                                   "cacheType",
                                                   "GROUP_TEAM",
                                                   "activityId",
                                                   groupTeam.getActivityId(),
                                                   "groupTeamId",
                                                   groupTeam.getGroupTeamId()
                                           ))
                                           .build();

        createMqTask("GROUP", "cache-refresh-topic", event);
    }

    @Override
    public void joinGroupTeam(Long activityId, Long groupTeamId) {
        groupTeamDao.joinGroupTeam(activityId, groupTeamId);

        // todo 发送延迟消息到 Redis 消息队列，实现记录过期处理
        EventEnvelope event = EventEnvelope.builder()
                                           .eventType(EventType.CACHE_REFRESH)
                                           .bizId(UUID.randomUUID()
                                                      .toString())
                                           .shardKey(activityId.toString())
                                           .payload(Map.of(
                                                   "cacheType",
                                                   "GROUP_TEAM",
                                                   "activityId",
                                                   activityId,
                                                   "groupTeamId",
                                                   groupTeamId
                                           ))
                                           .build();

        createMqTask("GROUP", "cache-refresh-topic", event);
    }

    @Override
    public void addActivityOrderRecord(ActivityOrderRecordEntity activityOrderRecord) {
        ActivityOrderRecordPo activityOrderRecordPo = ActivityOrderRecordPo.builder()
                                                                           .orderNo(activityOrderRecord.getOrderNo())
                                                                           .userId(activityOrderRecord.getUserId())
                                                                           .activityId(activityOrderRecord.getActivityId())
                                                                           .activityName(activityOrderRecord.getActivityName())
                                                                           .activityBusinessId(activityOrderRecord.getActivityBusinessId())
                                                                           .participationType(activityOrderRecord.getParticipationType()
                                                                                                                 .getCode())
                                                                           .quotaOccupied(activityOrderRecord.getQuotaOccupied())
                                                                           .recordStatus(activityOrderRecord.getRecordStatus())
                                                                           .joinTime(activityOrderRecord.getJoinTime())
                                                                           .expireTime(activityOrderRecord.getExpireTime())
                                                                           .build();

        activityOrderRecordDao.addActivityOrderRecord(activityOrderRecordPo);

        // todo 发送延迟消息到 Redis 消息队列，实现记录过期处理
        EventEnvelope event = EventEnvelope.builder()
                                           .eventType(EventType.CACHE_REFRESH)
                                           .bizId(UUID.randomUUID()
                                                      .toString())
                                           .shardKey(activityOrderRecordPo.getActivityId().toString())
                                           .payload(Map.of(
                                                   "cacheType",
                                                   "ACTIVITY_ORDER_RECORD",
                                                   "userId",
                                                   activityOrderRecordPo.getUserId(),
                                                   "activityId",
                                                   activityOrderRecordPo.getActivityId()
                                           ))
                                           .build();

        createMqTask("GROUP", "cache-refresh-topic", event);
    }

    @Override
    public void reserveSkuStock(Long skuId, int quantity) {
        skuDao.reserveSkuStock(skuId, quantity);

        // todo 发送延迟消息到 Redis 消息队列，实现记录过期处理

        EventEnvelope event = EventEnvelope.builder()
                                           .eventType(EventType.CACHE_REFRESH)
                                           .bizId(UUID.randomUUID()
                                                      .toString())
                                           .shardKey(skuId.toString())
                                           .payload(Map.of("cacheType", "SKU", "skuId", skuId))
                                           .build();

        createMqTask("SKU", "cache-refresh-topic", event);
    }

    @Override
    public void addOrderItem(OrderItemEntity orderItem) {
        TradeOrderItemPo tradeOrderItemPo = TradeOrderItemPo.builder()
                                                            .orderNo(orderItem.getOrderNo())
                                                            .skuId(orderItem.getSkuId())
                                                            .productName(orderItem.getProductName())
                                                            .originPrice(orderItem.getOriginPrice())
                                                            .quantity(orderItem.getQuantity())
                                                            .actualPrice(orderItem.getActualPrice())
                                                            .discountPrice(orderItem.getDiscountPrice())
                                                            .activityId(orderItem.getActivityId())
                                                            .activityType(orderItem.getActivityType())
                                                            .activityBusinessId(orderItem.getActivityBusinessId())
                                                            .build();
        tradeOrderItemDao.addOrderItem(tradeOrderItemPo);
        // 无缓存
    }

    @Override
    public void addTradeOrder(OrderAggregate order) {
        TradeOrderPo tradeOrderPo = TradeOrderPo.builder()
                                                .orderNo(order.getOrderNo())
                                                .userId(order.getUserTagRecord()
                                                             .getUserId())
                                                .userTag(order.getUserTagRecord().getUserTag())
                                                .orderStatus(order.getOrderStatus().getCode())
                                                .totalAmount(order.getTotalAmount())
                                                .payAmount(order.getPayAmount())
                                                .discountAmount(order.getDiscountAmount())
                                                .entrySource(order.getTradeSC().getEntrySource())
                                                .orderCreateTime(order.getOrderCreateTime())
                                                .orderExpireTime(order.getOrderExpireTime())
                                                .build();
        tradeOrderDao.addTradeOrder(tradeOrderPo);
        // 无缓存
    }

    /* 缓存预热：通过 BeanPostProcessor 实现，利用 @EventListener 定义监听到应用完全启动后自动执行的方法 */
    @EventListener(ApplicationReadyEvent.class)
    public void preloadHotData() {
        log.info("[仓储实现层] ========== 缓存预热开始 ==========");
        // 只预热配置项缓存：读多写少数据
        cacheManager.reloadConfigCache();
        log.info("[仓储实现层] ========== 缓存预热结束 ==========");
    }

    private void createMqTask(String bizType, String topic, EventEnvelope event) {
        MqTaskPo task = MqTaskPo.builder()
                                .bizId(event.getBizId())
                                .bizType(bizType)
                                .eventType(event.getEventType()
                                                .name())
                                .topic(topic)
                                .shardKey(event.getShardKey())
                                .payload(JSON.toJSONString(event))
                                .status(0)
                                .retryTimes(0)
                                .maxRetryTimes(3)
                                .build();

        mqTaskDao.insert(task);
    }

}
