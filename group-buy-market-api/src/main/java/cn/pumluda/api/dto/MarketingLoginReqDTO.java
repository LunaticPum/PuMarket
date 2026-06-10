package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Project: group-buy-market-better <p>
 * File: MarketingLoginReqDTO <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 营销后台登录请求 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MarketingLoginReqDTO {

    private String username;
    private String password;

}
