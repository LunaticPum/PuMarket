package cn.pumluda.types.event;

import cn.pumluda.types.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

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
@AllArgsConstructor
@NoArgsConstructor
public class EventEnvelope {

    private EventType eventType;

    private String bizId;

    private String shardKey;

    private Map<String, Object> payload;

}
