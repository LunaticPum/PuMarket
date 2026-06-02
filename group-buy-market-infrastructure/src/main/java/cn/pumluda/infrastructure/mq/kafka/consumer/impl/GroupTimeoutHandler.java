package cn.pumluda.infrastructure.mq.kafka.consumer.impl;

import cn.hutool.core.lang.TypeReference;
import cn.pumluda.infrastructure.dao.IGroupTeamDao;
import cn.pumluda.infrastructure.dao.ITradeOrderDao;
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
@Component("GROUP_TIMEOUT_CLOSE")
@Slf4j
public class GroupTimeoutHandler implements EventHandler {

    @Resource
    private IGroupTeamDao groupTeamDao;

    @Override
    public EventType support() {
        return EventType.GROUP_TIMEOUT_CLOSE;
    }

    @Override
    public void handle(EventEnvelope event) {
        Map<String, Object> payload =
                JSON.parseObject(
                        event.getPayload(),
                        new TypeReference<Map<String, Object>>() {
                        }
                );

        Long activityId = (Long) payload.get("activityId");
        Long groupTeamId = (Long) payload.get("groupTeamId");

        groupTeamDao.closeGroupTeam(activityId, groupTeamId);
    }
}
