package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: ActivityDetailResDTO <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 活动详情响应 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityDetailResDTO {

    // 活动基本信息
    private Long activityId;
    private String activityName;
    private Integer activityType;
    private String activityTypeName;
    private Integer discountType;
    private Integer totalDiscountQuota;
    private Integer status;
    private Date startTime;
    private Date endTime;

    // 队伍统计
    private int totalTeams;
    private List<TeamCard> teams;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TeamCard {

        private Long groupTeamId;
        private Long leaderUserId;
        private Long skuId;
        private int requiredNum;
        private int currentNum;
        private int settledTradeNum;
        private int teamStatus;
        private String teamStatusName;
        private Date teamCreateTime;
        private Date teamExpireTime;
    }

}
