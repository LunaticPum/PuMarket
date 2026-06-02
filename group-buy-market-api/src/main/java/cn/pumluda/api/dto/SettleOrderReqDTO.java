package cn.pumluda.api.dto;

import lombok.Data;


/**
 * Project: group-buy-market-better <p>
 * File: CreateOrderReqDTO <p>
 * Created by: 16374 <p>
 * Date: 2026/5/26 <p>
 * Time: 17:28 <p>
 * Description: 创建订单请求 DTO
 */
@Data
public class SettleOrderReqDTO {

    /* 用户 ID */
    private Long userId;

    /* 交易单号：由外部系统生成并传入 */
    private String orderNo;

    /* 流量来源 */
    private String payChannel;

}

