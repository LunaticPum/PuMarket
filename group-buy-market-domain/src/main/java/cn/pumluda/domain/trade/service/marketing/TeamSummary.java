package cn.pumluda.domain.trade.service.marketing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

/**
 * Project: group-buy-market-better <p>
 * File: TeamSummary <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 队伍摘要信息
 */
@Getter
@Builder
@AllArgsConstructor
public class TeamSummary {

    private final Long groupTeamId;
    private final Long leaderUserId;
    private final Long skuId;
    private final int requiredNum;
    private final int currentNum;
    private final int settledTradeNum;
    private final int teamStatus;
    private final String teamStatusName;
    private final Date teamCreateTime;
    private final Date teamExpireTime;

}
