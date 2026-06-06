package cn.pumluda.domain.trade.service.userQuery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Project: group-buy-market-better <p>
 * File: UserOrderItem <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 用户订单列表项（聚合结果）
 */
@Getter
@Builder
@AllArgsConstructor
public class UserOrderItem {

    private final String orderNo;
    private final Long skuId;
    private final String productName;
    private final BigDecimal originPrice;
    private final BigDecimal actualPrice;
    private final int quantity;
    private final int orderStatus;
    private final String displayStatus;
    private final String activityName;
    private final Long groupTeamId;
    private final Integer teamStatus;
    private final String teamProgress;
    private final Date orderCreateTime;

}
