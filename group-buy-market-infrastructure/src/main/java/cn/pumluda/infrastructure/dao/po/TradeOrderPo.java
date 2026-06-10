package cn.pumluda.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Project: group-buy-market-better <p>
 * File: TradeOrderPo <p>
 * Created by: 16374 <p>
 * Date: 2026/5/30 <p>
 * Time: 15:27 <p>
 * Description: 交易订单主表 PO
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeOrderPo {

    /* 自增主键 */
    private Long id;

    /* 交易单号 */
    private String orderNo;
    /* 用户 ID */
    private Long userId;
    /* 用户标签 */
    private Integer userTag;

    /* 订单状态 */
    private Integer orderStatus;
    /* 原始总金额 */
    private BigDecimal totalAmount;
    /* 支付金额 */
    private BigDecimal payAmount;
    /* 总优惠金额 */
    private BigDecimal discountAmount;

    /* 流量来源 */
    private Integer entrySource;
    /* 支付渠道 */
    private String payChannel;

    /* 订单创建时间 */
    private Date orderCreateTime;
    /* 订单过期时间 */
    private Date orderExpireTime;
    /* 订单结束时间 */
    private Date orderEndTime;

    /* 记录创建时间 */
    private Date createTime;
    /* 记录更新时间 */
    private Date updateTime;

}
