package cn.pumluda.domain.trade.service.marketing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

/**
 * Project: group-buy-market-better <p>
 * File: ActivityOverview <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 活动总览统计结果
 */
@Getter
@Builder
@AllArgsConstructor
public class ActivityOverview {

    private final Long activityId;
    private final String activityName;
    private final Integer activityType;
    private final int totalTeams;
    private final int activeTeams;
    private final int completedTeams;
    private final int failedTeams;
    private final int totalParticipants;
    private final int settledOrders;
    private final String settlementRate;
    private final Integer remainingQuota;
    private final Date startTime;
    private final Date endTime;

}
