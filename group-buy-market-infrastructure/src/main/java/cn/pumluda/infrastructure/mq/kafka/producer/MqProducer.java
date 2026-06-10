package cn.pumluda.infrastructure.mq.kafka.producer;

import cn.pumluda.types.event.EventEnvelope;
import com.alibaba.fastjson.JSON;
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
public class MqProducer {

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, EventEnvelope event) {
        String payLoad = JSON.toJSONString(event);
        kafkaTemplate.send(topic, event.getShardKey(), payLoad);

        log.info(
                "发送MQ消息 topic={} eventType={} bizId={}",
                topic,
                event.getEventType(),
                event.getBizId()
        );
    }

}
