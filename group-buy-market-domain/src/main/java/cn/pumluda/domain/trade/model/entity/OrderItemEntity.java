package cn.pumluda.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Project: group-buy-market-better <p>
 * File: OrderItemEntity <p>
 * Created by: 16374 <p>
 * Date: 2026/5/27 <p>
 * Time: 10:22 <p>
 * Description: 订单明细
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemEntity {

    /* 交易单号 */
    private String orderNo;

    /* 商品 SKU id */
    private Long skuId;
    /* 商品名称 */
    private String productName;
    /* 商品原始单价 */
    private BigDecimal price;
    /* 商品购买数量 */
    private Integer quantity;
    /* 实际支付价格（有优惠就总价扣减优惠金额） */
    private BigDecimal actualPrice;

    /* 活动 ID */
    private Long activityId;
    /* 拼团队伍 ID */
    private Long groupTeamId;

}
