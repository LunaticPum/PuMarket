package cn.pumluda.infrastructure.mq.kafka.consumer.impl;

import cn.hutool.core.lang.TypeReference;
import cn.pumluda.infrastructure.mq.kafka.consumer.EventHandler;
import cn.pumluda.types.enums.EventType;
import cn.pumluda.types.event.EventEnvelope;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Project: group-buy-market-better <p>
 * File: SkuStockExhaustedHandler <p>
 * Created by: 16374 <p>
 * Date: 2026/6/2 <p>
 * Time: 13:38 <p>
 * Description: 库存告罄 handler
 */
@Component("SKU_STOCK_EXHAUSTED")
@Slf4j
public class SkuStockExhaustedHandler implements EventHandler {

    @Override
    public EventType support() {
        return EventType.SKU_STOCK_EXHAUSTED;
    }

    @Override
    public void handle(EventEnvelope event) {

        Map<String, Object> payload = event.getPayload();
        log.warn(
                "商品库存告罄 skuId={}",
                payload.get("skuId")
        );

        // TODO
        // 发送邮件
        // 发送短信
        // RPC补货通知
    }
}
