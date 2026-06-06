package cn.pumluda.domain.trade.service.userQuery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Project: group-buy-market-better <p>
 * File: UserOrderDetailResult <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 用户订单详情查询结果
 */
@Getter
@Builder
@AllArgsConstructor
public class UserOrderDetailResult {

    private final String orderNo;
    private final Long userId;
    private final Long skuId;
    private final String productName;
    private final BigDecimal originPrice;
    private final BigDecimal actualPrice;
    private final BigDecimal discountPrice;
    private final int quantity;
    private final int orderStatus;
    private final String displayStatus;
    private final Date orderCreateTime;
    private final Date orderExpireTime;
    private final Long activityId;
    private final String activityName;
    private final Integer activityType;
    private final TeamInfo teamInfo;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class TeamInfo {
        private final Long groupTeamId;
        private final Long leaderUserId;
        private final int requiredNum;
        private final int currentNum;
        private final int settledTradeNum;
        private final int teamStatus;
        private final String teamStatusName;
        private final int remainingSlots;
        private final Date teamCreateTime;
        private final Date teamExpireTime;
    }

}
