package cn.pumluda.infrastructure.mq.kafka.consumer.impl;

import cn.pumluda.infrastructure.cache.ICacheManager;
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
@Component("CACHE_REFRESH")
@Slf4j
public class CacheRefreshHandler implements EventHandler {

    @Resource
    private ICacheManager cacheManager;

    @Override
    public EventType support() {
        return EventType.CACHE_REFRESH;
    }

    @Override
    public void handle(EventEnvelope event) {
        Map<String, Object> payload = event.getPayload();

        String cacheType = (String) payload.get("cacheType");

        switch (cacheType) {
            case "GROUP_TEAM":
                log.info("收到缓存刷新消息: {}", JSON.toJSONString(event));
                cacheManager.refreshGroupTeamCache(
                        ((Number) payload.get("activityId")).longValue(),
                        ((Number) payload.get("groupTeamId")).longValue()
                );
                break;
            case "ACTIVITY_ORDER_RECORD":
                log.info("收到缓存刷新消息: {}", JSON.toJSONString(event));
                cacheManager.refreshActivityOrderRecordCache(
                        ((String) payload.get("orderNo")),
                        ((Number) payload.get("activityId")).longValue()
                );
                break;
            case "SKU":
                log.info("收到缓存刷新消息: {}", JSON.toJSONString(event));
                cacheManager.refreshSkuCache(
                        ((Number) payload.get("skuId")).longValue()
                );
                break;
            case "ACTIVITY_CONFIG":
                log.info("收到缓存刷新消息: {}", JSON.toJSONString(event));
                cacheManager.refreshActivityCache(
                        ((Number) payload.get("activityId")).longValue()
                );
                break;
        }
    }
}
