package cn.pumluda.domain.trade.model.aggregate;

import cn.pumluda.domain.trade.model.entity.UserTagRecordEntity;
import cn.pumluda.domain.trade.model.valobj.OrderStatusEnumVo;
import cn.pumluda.domain.trade.model.valobj.TradeSCVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Project: group-buy-market-better <p>
 * File: OrderItemEntity <p>
 * Created by: 16374 <p>
 * Date: 2026/5/27 <p>
 * Time: 10:04 <p>
 * Description: 订单主表 —— 用于持久化
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderAggregate {

    /* 交易单号 */
    private String orderNo;
    /* 用户标签记录：标签可为空，只有部分被打上标签的用户会有 */
    private UserTagRecordEntity userTagRecord;
    /* 交易状态 */
    private OrderStatusEnumVo orderStatus;
    /* 原始总价 */
    private BigDecimal totalAmount;
    /* 实际总支付金额 */
    private BigDecimal payAmount;
    /* 总优惠金额 */
    private BigDecimal discountAmount;

    /* 交易流量入口和支付渠道 */
    private TradeSCVo tradeSC;

    /* 订单创建时间 */
    private Date orderCreateTime;
    /* 订单过期时间 */
    private Date orderExpireTime;
    /* 订单结束时间 */
    private Date orderEndTime;

}
