package cn.pumluda.infrastructure.mq.kafka.consumer;

import cn.pumluda.types.enums.EventType;
import cn.pumluda.types.event.EventEnvelope;

/**
 * Project: group-buy-market-better <p>
 * File: EventHandler <p>
 * Created by: 16374 <p>
 * Date: 2026/6/2 <p>
 * Time: 13:10 <p>
 * Description: 事件消费者抽象
 */
public interface EventHandler {

    /* 当前 Handler 支持的事件 */
    EventType support();

    /* 处理事件 */
    void handle(EventEnvelope event);

}
