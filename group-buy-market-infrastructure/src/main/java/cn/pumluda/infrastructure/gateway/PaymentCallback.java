package cn.pumluda.infrastructure.gateway;

import cn.pumluda.infrastructure.dao.*;
import cn.pumluda.infrastructure.dao.po.ActivityOrderRecordPo;
import cn.pumluda.infrastructure.dao.po.GroupTeamPo;
import cn.pumluda.infrastructure.dao.po.MqTaskPo;
import cn.pumluda.infrastructure.dao.po.TradeOrderItemPo;
import cn.pumluda.types.enums.EventType;
import cn.pumluda.types.event.EventEnvelope;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.UUID;

/**
 * Project: group-buy-market-better <p>
 * File: PaymentCallback <p>
 * Created by: 16374 <p>
 * Date: 2026/6/2 <p>
 * Time: 17:26 <p>
 * Description: 支付回调
 */
@Service
public class PaymentCallback {

    @Resource
    private IMqTaskDao mqTaskDao;
    @Resource
    private ITradeOrderDao orderDao;
    @Resource
    private ITradeOrderItemDao orderItemDao;
    @Resource
    private IGroupTeamDao groupTeamDao;
    @Resource
    private IActivityOrderRecordDao activityOrderRecordDao;
    @Resource
    private ISkuDao skuDao;

    public void settleTrade(String orderNo, Long userId) {
        orderDao.settleTradeOrder(orderNo, userId);
        TradeOrderItemPo orderItem = orderItemDao.getOrderItem(orderNo);

        Long skuId = orderItem.getSkuId();
        int quantity = orderItem.getQuantity();
        Long groupTeamId = orderItem.getActivityBusinessId();
        Long activityId = orderItem.getActivityId();

        skuDao.consumeLockedStockBySkuId(skuId, quantity);
        groupTeamDao.addSettledNum(activityId, groupTeamId);
        activityOrderRecordDao.settleActivityOrder(userId, activityId);

        GroupTeamPo groupTeam = groupTeamDao.getGroupTeam(activityId, groupTeamId);
        ActivityOrderRecordPo activityOrderRecordPo = activityOrderRecordDao.getActivityOrderRecord(userId, activityId);

        EventEnvelope event1 = EventEnvelope.builder()
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

        createMqTask("GROUP", "cache-refresh-topic", event1);

        EventEnvelope event2 = EventEnvelope.builder()
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
        createMqTask("GROUP", "cache-refresh-topic", event2);

        EventEnvelope event3 = EventEnvelope.builder()
                                            .eventType(EventType.CACHE_REFRESH)
                                            .bizId(UUID.randomUUID()
                                                       .toString())
                                            .shardKey(skuId.toString())
                                            .payload(Map.of("cacheType", "SKU", "skuId", skuId))
                                            .build();

        createMqTask("SKU", "cache-refresh-topic", event3);
    }

    public void cancelTrade(String orderNo, Long userId) {
        orderDao.cancelTradeOrder(orderNo, userId);
        TradeOrderItemPo orderItem = orderItemDao.getOrderItem(orderNo);

        Long skuId = orderItem.getSkuId();
        int quantity = orderItem.getQuantity();
        Long groupTeamId = orderItem.getActivityBusinessId();
        Long activityId = orderItem.getActivityId();

        skuDao.repairStockBySkuId(skuId, quantity);
        groupTeamDao.repairQuota(activityId, groupTeamId);

        GroupTeamPo groupTeam = groupTeamDao.getGroupTeam(activityId, groupTeamId);
        if (null != groupTeam && groupTeam.getLeaderUserId().equals(userId)) {
            groupTeamDao.closeGroupTeam(activityId, groupTeamId);

        }
        ActivityOrderRecordPo activityOrderRecordPo = activityOrderRecordDao.getActivityOrderRecord(userId, activityId);
        activityOrderRecordDao.closeActivityOrder(userId, activityId);

        EventEnvelope event1 = EventEnvelope.builder()
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

        createMqTask("GROUP", "cache-refresh-topic", event1);

        EventEnvelope event2 = EventEnvelope.builder()
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
        createMqTask("GROUP", "cache-refresh-topic", event2);

        EventEnvelope event3 = EventEnvelope.builder()
                                            .eventType(EventType.CACHE_REFRESH)
                                            .bizId(UUID.randomUUID()
                                                       .toString())
                                            .shardKey(skuId.toString())
                                            .payload(Map.of("cacheType", "SKU", "skuId", skuId))
                                            .build();

        createMqTask("SKU", "cache-refresh-topic", event3);
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
