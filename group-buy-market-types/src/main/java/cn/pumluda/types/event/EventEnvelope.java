package cn.pumluda.types.event;

import cn.pumluda.types.enums.EventType;
import lombok.Builder;
import lombok.Data;

/**
 * Project: group-buy-market-better <p>
 * File: EventEncelope <p>
 * Created by: 16374 <p>
 * Date: 2026/6/2 <p>
 * Time: 12:47 <p>
 * Description: 统一通知事件模型
 */
@Data
@Builder
public class EventEnvelope {

    private EventType eventType;

    private String bizId;

    private String shardKey;

    private String payload;

}
