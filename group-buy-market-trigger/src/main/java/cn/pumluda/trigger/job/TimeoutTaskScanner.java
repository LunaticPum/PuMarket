package cn.pumluda.trigger.job;

import cn.pumluda.infrastructure.dao.IActivityOrderRecordDao;
import cn.pumluda.infrastructure.dao.IGroupTeamDao;
import cn.pumluda.infrastructure.dao.IMqTaskDao;
import cn.pumluda.infrastructure.dao.ITradeOrderDao;
import cn.pumluda.infrastructure.dao.po.ActivityOrderRecordPo;
import cn.pumluda.infrastructure.dao.po.GroupTeamPo;
import cn.pumluda.infrastructure.dao.po.MqTaskPo;
import cn.pumluda.infrastructure.dao.po.TradeOrderPo;
import cn.pumluda.types.enums.EventType;
import cn.pumluda.types.event.EventEnvelope;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Project: group-buy-market-better <p>
 * File: TimeoutTaskDispatcher <p>
 * Created by: 16374 <p>
 * Date: 2026/6/2 <p>
 * Time: 11:42 <p>
 * Description: 超时任务扫描
 */
@Component
@Slf4j
public class TimeoutTaskScanner {

    @Resource
    private ITradeOrderDao orderDao;

    @Resource
    private IGroupTeamDao groupTeamDao;

    @Resource
    private IActivityOrderRecordDao activityOrderRecordDao;

    @Resource
    private IMqTaskDao mqTaskDao;

    @Scheduled(fixedDelay = 3000)
    public void scan() {

        // 1. 超时订单
        List<TradeOrderPo> timeoutOrders = orderDao.queryTimeoutOrders();

        for (TradeOrderPo order : timeoutOrders) {
            if (order.getOrderStatus() == 0) {
                EventEnvelope event =
                        EventEnvelope.builder()
                                     .eventType(EventType.ORDER_TIMEOUT_CLOSE)
                                     .bizId("ORDER_TIMEOUT:" + order.getOrderNo())
                                     .shardKey(order.getUserId().toString())
                                     .payload(JSON.toJSONString(
                                             Map.of(
                                                     "orderNo", order.getOrderNo(),
                                                     "userId", order.getUserId()
                                             )
                                     ))
                                     .build();

                createTask(
                        "ORDER",
                        "order-timeout-topic",
                        event
                );
            }
        }

        // 2. 超时拼团
        List<GroupTeamPo> timeoutGroups = groupTeamDao.queryTimeoutGroups();

        for (GroupTeamPo group : timeoutGroups) {
            if (group.getTeamStatus() == 0) {
                EventEnvelope event =
                        EventEnvelope.builder()
                                     .eventType(EventType.GROUP_TIMEOUT_CLOSE)
                                     .bizId("GROUP_TIMEOUT:" + group.getGroupTeamId())
                                     .shardKey(group.getActivityId().toString())
                                     .payload(JSON.toJSONString(
                                             Map.of(
                                                     "activityId", group.getActivityId(),
                                                     "groupTeamId", group.getGroupTeamId()
                                             )
                                     ))
                                     .build();

                createTask(
                        "GROUP",
                        "group-timeout-topic",
                        event
                );
            }
        }

        // 3. 超时活动订单记录
        List<ActivityOrderRecordPo> timeoutRecords = activityOrderRecordDao.queryTimeoutRecord();

        for (ActivityOrderRecordPo record : timeoutRecords) {
            if (record.getRecordStatus() == 0) {
                EventEnvelope event =
                        EventEnvelope.builder()
                                     .eventType(EventType.ACTIVITY_ORDER_RECORD_TIMEOUT_CLOSE)
                                     .bizId("ACTIVITY_ORDER_RECORD_TIMEOUT:" + record.getOrderNo())
                                     .shardKey(record.getActivityId().toString())
                                     .payload(JSON.toJSONString(
                                             Map.of(
                                                     "activityId",
                                                     record.getActivityId(),
                                                     "userId",
                                                     record.getUserId()
                                             )
                                     ))
                                     .build();

                createTask(
                        "ACTIVITY",
                        "activity-order-record-timeout-topic",
                        event
                );
            }
        }
    }

    private void createTask(
            String bizType,
            String topic,
            EventEnvelope event
    ) {
        boolean exists = mqTaskDao.exists(event.getBizId());
        if (exists) return;

        MqTaskPo task = MqTaskPo.builder()
                                .bizId(event.getBizId())
                                .bizType(bizType)
                                .eventType(event.getEventType().name())
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
