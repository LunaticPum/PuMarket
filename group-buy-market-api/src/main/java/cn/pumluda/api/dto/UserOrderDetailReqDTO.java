package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Project: group-buy-market-better <p>
 * File: UserOrderDetailReqDTO <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 用户订单详情请求 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserOrderDetailReqDTO {

    /* 交易单号 */
    private String orderNo;

    /* 用户 ID */
    private Long userId;

}
