package cn.pumluda.infrastructure.mq.kafka.producer;

import cn.pumluda.types.event.CacheRefreshEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Project: group-buy-market-better <p>
 * File: CacheProducer <p>
 * Created by: 16374 <p>
 * Date: 2026/6/2 <p>
 * Time: 08:20 <p>
 * Description: 缓存相关事件发布
 */
@Component
@Slf4j
public class CacheRefreshProducer {

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void send(CacheRefreshEvent event) {
        kafkaTemplate.send("cache-refresh-topic", event.getShardKey(), event);

        log.info("发送缓存刷新消息 cacheType={} event={}", event.getCacheType().name(), event);
    }

}
