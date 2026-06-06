package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Project: group-buy-market-better <p>
 * File: MarketingLoginResDTO <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 营销后台登录响应 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MarketingLoginResDTO {

    private String token;
    private String username;
    private String role;

}
