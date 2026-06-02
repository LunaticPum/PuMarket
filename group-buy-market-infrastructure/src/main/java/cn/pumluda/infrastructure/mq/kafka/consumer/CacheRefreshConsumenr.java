package cn.pumluda.infrastructure.mq.kafka.consumer;

import cn.pumluda.infrastructure.cache.ICacheManager;
import cn.pumluda.types.event.CacheRefreshEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Project: group-buy-market-better <p>
 * File: CacheRefreshConsumenr <p>
 * Created by: 16374 <p>
 * Date: 2026/6/2 <p>
 * Time: 08:40 <p>
 * Description: 写缓存事件消费
 */
@Component
@Slf4j
public class CacheRefreshConsumenr {

    @Resource
    private ICacheManager cacheManager;

    @KafkaListener(topics = "cache-refresh-topic", groupId = "cache-group")
    public void consume(CacheRefreshEvent event) {
        switch (event.getCacheType()) {
            case GROUP_TEAM:
                cacheManager.refreshGroupTeamCache(event.getActivityId(), event.getGroupTeamId());
                break;
            case ACTIVITY_ORDER_RECORD:
                cacheManager.refreshActivityOrderRecordCache(event.getUserId(), event.getActivityId());
                break;
            case SKU:
                cacheManager.refreshSkuCache(event.getSkuId());
                break;
            case ACTIVITY_CONFIG:
                cacheManager.refreshActivityCache(event.getActivityId());
                break;
        }
    }

}
