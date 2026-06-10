package cn.pumluda.infrastructure.mq.kafka.consumer.impl;

import cn.pumluda.infrastructure.gateway.ProductRPC;
import cn.pumluda.infrastructure.mq.kafka.consumer.EventHandler;
import cn.pumluda.types.enums.EventType;
import cn.pumluda.types.event.EventEnvelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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

    @Resource
    private ProductRPC productRPC;

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
        productRPC.restockProductBySkuId((Long) payload.get("skuId"));
        log.info(
                "商品补货成功，补充 100 件商品 skuId={}",
                payload.get("skuId")
        );

    }
}
