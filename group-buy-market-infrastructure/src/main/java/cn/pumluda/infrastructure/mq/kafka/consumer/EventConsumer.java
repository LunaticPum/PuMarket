package cn.pumluda.infrastructure.mq.kafka.consumer;

import cn.pumluda.types.event.EventEnvelope;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Project: group-buy-market-better <p>
 * File: EventConsumer <p>
 * Created by: 16374 <p>
 * Date: 2026/6/2 <p>
 * Time: 13:07 <p>
 * Description: 按策略模式动态注入消息消费者
 */
@Component
@Slf4j
public class EventConsumer {

    @Resource
    private Map<String, EventHandler> eventHandlerMap;

    @KafkaListener(
            topics = {
                    "cache-refresh-topic",
                    "restock-topic"
            },
            groupId = "biz-group"
    )
    public void consume(String message) {
        EventEnvelope event = JSON.parseObject(message, EventEnvelope.class);

        EventHandler handler = eventHandlerMap.get(event.getEventType().name());

        handler.handle(event);
    }

}
