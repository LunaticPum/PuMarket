package cn.pumluda.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Project: group-buy-market-better <p>
 * File: MarketingUserPo <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 营销后台用户 PO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MarketingUserPo {

    private Long id;
    private String username;
    private String passwordHash;
    private String role;
    private Date createTime;
    private Date updateTime;

}
