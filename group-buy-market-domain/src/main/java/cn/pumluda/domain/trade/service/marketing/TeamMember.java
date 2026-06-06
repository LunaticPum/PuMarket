package cn.pumluda.domain.trade.service.marketing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

/**
 * Project: group-buy-market-better <p>
 * File: TeamMember <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 队伍成员信息
 */
@Getter
@Builder
@AllArgsConstructor
public class TeamMember {

    private final String orderNo;
    private final Long userId;
    private final Integer participationType;
    private final String participationTypeName;
    private final String orderStatus;
    private final Date joinTime;

}
