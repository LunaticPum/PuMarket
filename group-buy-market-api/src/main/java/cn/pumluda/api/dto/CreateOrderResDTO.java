package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Project: group-buy-market-better <p>
 * File: CreateOrderResDTO <p>
 * Created by: 16374 <p>
 * Date: 2026/5/26 <p>
 * Time: 17:28 <p>
 * Description: 创建订单响应 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderResDTO {

    /* 交易单号 */
    private String orderNo;

    /* 实际支付金额 */
    private BigDecimal payPrice;

    /* 订单状态 */
    private Integer orderStatus;

}
