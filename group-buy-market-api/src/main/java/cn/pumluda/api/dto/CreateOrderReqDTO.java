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
public class CreateOrderReqDTO {

    /* 用户 ID */
    private Long userId;

    /* 商品 ID */
    private Long skuId;

    /* 活动 ID */
    private Long activityId;

    /* 拼团队伍 ID */
    private Long groupTeamId;

    /* 流量来源 */
    private Integer entrySource;

    /* 交易渠道 */
    private String channel;

}

