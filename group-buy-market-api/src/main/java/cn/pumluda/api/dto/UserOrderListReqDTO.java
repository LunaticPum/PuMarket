package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Project: group-buy-market-better <p>
 * File: UserOrderListReqDTO <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 用户订单列表请求 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserOrderListReqDTO {

    /* 用户 ID */
    private Long userId;

    /* 页码，从 1 开始，默认 1 */
    private Integer page;

    /* 每页条数，默认 10 */
    private Integer size;

}
