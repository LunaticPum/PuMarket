package cn.pumluda.domain.trade.service.preCheck.dto;

import cn.pumluda.domain.trade.model.aggregate.OrderAggregate;
import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.SkuEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Project: group-buy-market-better <p>
 * File: CreateOrderPreCheckResult <p>
 * Created by: 16374 <p>
 * Date: 2026/5/27 <p>
 * Time: 18:33 <p>
 * Description: 前置校验结果 DTO
 */
@Getter
@AllArgsConstructor
public class PreCheckResult {
    private final boolean firstCreate;
    private final OrderAggregate order;
    private final SkuEntity sku;
    private final ActivityConfigEntity activityConfig;
    private final String bizId;
}
