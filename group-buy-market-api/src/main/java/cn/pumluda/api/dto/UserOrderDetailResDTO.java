package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Project: group-buy-market-better <p>
 * File: UserOrderDetailResDTO <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 用户订单详情响应 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserOrderDetailResDTO {

    /* 交易单号 */
    private String orderNo;

    /* 用户 ID */
    private Long userId;

    /* 商品名称 */
    private String productName;

    /* 商品主图 */
    private String imageUrl;

    /* 商品原单价 */
    private BigDecimal originPrice;

    /* 实际支付金额 */
    private BigDecimal actualPrice;

    /* 优惠金额 */
    private BigDecimal discountPrice;

    /* 购买数量 */
    private int quantity;

    /* 订单状态 */
    private int orderStatus;

    /* 前端展示状态 */
    private String displayStatus;

    /* 订单创建时间 */
    private Date orderCreateTime;

    /* 订单过期时间 */
    private Date orderExpireTime;

    /* 活动 ID */
    private Long activityId;

    /* 活动名称 */
    private String activityName;

    /* 活动类型 */
    private Integer activityType;

    /* 拼团队伍信息（仅拼团订单） */
    private TeamInfo teamInfo;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TeamInfo {
        private Long groupTeamId;
        private Long leaderUserId;
        private int requiredNum;
        private int currentNum;
        private int settledTradeNum;
        private int teamStatus;
        private String teamStatusName;
        private int remainingSlots;
        private Date teamCreateTime;
        private Date teamExpireTime;
    }

}
