package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Project: group-buy-market-better <p>
 * File: SalesOverviewResDTO <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 销售总览响应 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesOverviewResDTO {

    /* 今日订单数 */
    private int todayOrderCount;

    /* 今日交易总额 */
    private BigDecimal todayTotalAmount;

    /* 昨日订单数 */
    private int yesterdayOrderCount;

    /* 昨日交易总额 */
    private BigDecimal yesterdayTotalAmount;

}
