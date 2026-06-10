package cn.pumluda.domain.trade.service.marketing;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Project: group-buy-market-better <p>
 * File: SalesOverviewResult <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 销售总览结果
 */
@Getter
@AllArgsConstructor
public class SalesOverviewResult {

    private final int todayOrderCount;
    private final BigDecimal todayTotalAmount;
    private final int yesterdayOrderCount;
    private final BigDecimal yesterdayTotalAmount;

}
