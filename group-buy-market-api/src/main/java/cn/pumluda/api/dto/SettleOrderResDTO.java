package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Project: group-buy-market-better <p>
 * File: SettleOrderResDTO <p>
 * Created by: 16374 <p>
 * Date: 2026/5/26 <p>
 * Time: 17:28 <p>
 * Description: 订单结算/取消 响应 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettleOrderResDTO {

    /* 交易单号 */
    private String orderNo;

    /* 用户 ID */
    private Long userId;

    /* 操作后订单状态：1-已完成，2-已关闭 */
    private Integer orderStatus;

}
