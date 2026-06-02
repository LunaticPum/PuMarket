package cn.pumluda.infrastructure.mq.kafka.consumer.impl;

import cn.hutool.core.lang.TypeReference;
import cn.pumluda.infrastructure.dao.IActivityOrderRecordDao;
import cn.pumluda.infrastructure.dao.IGroupTeamDao;
import cn.pumluda.infrastructure.dao.po.ActivityOrderRecordPo;
import cn.pumluda.infrastructure.mq.kafka.consumer.EventHandler;
import cn.pumluda.types.enums.EventType;
import cn.pumluda.types.event.EventEnvelope;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Project: group-buy-market-better <p>
 * File: CacheRefreshHandler <p>
 * Created by: 16374 <p>
 * Date: 2026/6/2 <p>
 * Time: 13:12 <p>
 * Description: 缓存刷新事件
 */
@Component("ACTIVITY_ORDER_RECORD_TIMEOUT_CLOSE")
@Slf4j
public class ActivityOrderRecordTimeoutHandler implements EventHandler {

    @Resource
    private IActivityOrderRecordDao activityOrderRecordDao;
    @Resource
    private IGroupTeamDao groupTeamDao;

    @Override
    public EventType support() {
        return EventType.ACTIVITY_ORDER_RECORD_TIMEOUT_CLOSE;
    }

    @Override
    public void handle(EventEnvelope event) {
        Map<String, Object> payload =
                JSON.parseObject(
                        event.getPayload(),
                        new TypeReference<Map<String, Object>>() {
                        }
                );

        Long userId = (Long) payload.get("userId");
        Long activityId = (Long) payload.get("activityId");

        ActivityOrderRecordPo activityOrderRecord = activityOrderRecordDao.getActivityOrderRecord(userId, activityId);
        if (activityOrderRecord.getParticipationType() == 1) {
            groupTeamDao.closeGroupTeam(
                    activityOrderRecord.getActivityId(),
                    activityOrderRecord.getActivityBusinessId()
            );
        } else if (activityOrderRecord.getParticipationType() == 2) {
            groupTeamDao.repairQuota(
                    activityOrderRecord.getActivityId(),
                    activityOrderRecord.getActivityBusinessId()
            );
        }

        activityOrderRecordDao.closeActivityOrder(userId, activityId);
    }
}
