package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: UserOrderListResDTO <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 用户订单列表响应 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserOrderListResDTO {

    private int total;
    private int page;
    private int size;
    private List<OrderCard> list;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderCard {

        /* 交易单号 */
        private String orderNo;

        /* 商品名称 */
        private String productName;

        /* 商品主图 */
        private String imageUrl;

        /* 商品原单价 */
        private BigDecimal originPrice;

        /* 实际支付单价 */
        private BigDecimal actualPrice;

        /* 购买数量 */
        private int quantity;

        /* 订单状态：0-待支付，1-已完成，2-已取消 */
        private int orderStatus;

        /* 前端展示状态：待支付/待成团/已发货/已取消/拼团失败 */
        private String displayStatus;

        /* 参与的活动名称 */
        private String activityName;

        /* 拼团队伍 ID */
        private Long groupTeamId;

        /* 队伍状态 */
        private Integer teamStatus;

        /* 成团进度，格式 "2/3" */
        private String teamProgress;

        /* 订单创建时间 */
        private Date orderCreateTime;
    }

}
