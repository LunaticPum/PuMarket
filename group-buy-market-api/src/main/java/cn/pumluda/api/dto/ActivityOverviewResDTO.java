package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: ActivityOverviewResDTO <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 活动总览响应 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityOverviewResDTO {

    private List<ActivityStat> list;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActivityStat {

        private Long activityId;
        private String activityName;
        private Integer activityType;
        private int totalTeams;
        private int activeTeams;
        private int completedTeams;
        private int failedTeams;
        private int totalParticipants;
        private int settledOrders;
        private String settlementRate;
        private Integer remainingQuota;
        private Date startTime;
        private Date endTime;
    }

}
