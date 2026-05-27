package cn.pumluda.domain.trade.service.createOrder.dto;

import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.SkuEntity;
import cn.pumluda.types.enums.ResponseEnum;
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
public class CreateOrderPreCheckResult {
    private final boolean firstCreate;
    private final SkuEntity sku;
    private final ActivityConfigEntity activityConfig;
}
