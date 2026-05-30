package cn.pumluda.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Project: group-buy-market-better <p>
 * File: TradeOrderItemPo <p>
 * Created by: 16374 <p>
 * Date: 2026/5/30 <p>
 * Time: 15:27 <p>
 * Description: 交易订单明细 PO
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeOrderItemPo {

    /* 自增主键 */
    private Long id;

    /* 交易单号 */
    private String orderNo;
    /* 商品 SKU ID */
    private Long skuId;
    /* 商品名称 */
    private String productName;
    /* 商品原始单价 */
    private BigDecimal originPrice;
    /* 商品购买数量 */
    private int quantity;
    /* 实际价格（如果由优惠则算上优惠） */
    private BigDecimal actualPrice;

    /* 活动 ID */
    private Long activityId;
    /* 活动类型：0-默认活动，1-拼团，2-凑单 */
    private Integer activityType;
    /* 活动业务 ID */
    private Long activityBusinessId;

}
