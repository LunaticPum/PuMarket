package cn.pumluda.domain.trade.service.marketing;

import cn.pumluda.domain.trade.adapter.repository.ITradeRepository;
import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.ActivityOrderRecordEntity;
import cn.pumluda.domain.trade.model.entity.GroupTeamEntity;
import cn.pumluda.domain.trade.model.entity.OrderItemEntity;
import cn.pumluda.domain.trade.model.valobj.GroupTeamStatusEnumVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: MarketingQueryService <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 营销数据查询领域服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketingQueryService {

    private final ITradeRepository repository;

    /**
     * 活动总览 —— 所有活动及其队伍统计
     */
    public List<ActivityOverview> getActivityOverview() {
        List<ActivityConfigEntity> activities = repository.getAllActiveActivities();
        List<ActivityOverview> result = new ArrayList<>();

        for (ActivityConfigEntity activity : activities) {
            List<GroupTeamEntity> allTeams = repository.getTeamsByActivityId(
                    activity.getActivityId(), 0, Integer.MAX_VALUE);

            int totalTeams = allTeams.size();
            int activeTeams = 0;
            int completedTeams = 0;
            int failedTeams = 0;
            int totalParticipants = 0;
            int settledOrders = 0;

            for (GroupTeamEntity team : allTeams) {
                GroupTeamStatusEnumVo status = team.getTeamStatus();
                if (GroupTeamStatusEnumVo.PROGRESS.equals(status)) {
                    activeTeams++;
                } else if (GroupTeamStatusEnumVo.COMPLETE.equals(status)) {
                    completedTeams++;
                } else {
                    failedTeams++;
                }
                totalParticipants += team.getCurrentNum() != null ? team.getCurrentNum() : 0;
                settledOrders += team.getSettledTradeNum() != null ? team.getSettledTradeNum() : 0;
            }

            // 剩余名额 = 总名额 - 已占用
            Integer remainingQuota = null;
            if (activity.getTotalDiscountQuota() != null && activity.getUsedDiscountQuota() != null) {
                remainingQuota = activity.getTotalDiscountQuota() - activity.getUsedDiscountQuota();
            }

            // 结算率 = 已结算数 / 参与人数
            String settlementRate = totalParticipants > 0
                    ? String.format("%.1f%%", settledOrders * 100.0 / totalParticipants)
                    : "0%";

            result.add(ActivityOverview.builder()
                    .activityId(activity.getActivityId())
                    .activityName(activity.getActivityName())
                    .activityType(activity.getActivityType())
                    .totalTeams(totalTeams)
                    .activeTeams(activeTeams)
                    .completedTeams(completedTeams)
                    .failedTeams(failedTeams)
                    .totalParticipants(totalParticipants)
                    .settledOrders(settledOrders)
                    .settlementRate(settlementRate)
                    .remainingQuota(remainingQuota)
                    .startTime(activity.getStartTime())
                    .endTime(activity.getEndTime())
                    .build());
        }

        return result;
    }

    /**
     * 活动详情 —— 活动信息 + 分页队伍列表
     */
    public ActivityDetailResult getActivityDetail(Long activityId, int page, int size) {
        ActivityConfigEntity activity = repository.getActivityByActivityId(activityId);
        if (activity == null) {
            return null;
        }

        int offset = (page - 1) * size;
        List<GroupTeamEntity> teams = repository.getTeamsByActivityId(activityId, offset, size);
        int totalTeams = repository.countTeamsByActivityId(activityId);

        List<TeamSummary> teamSummaries = new ArrayList<>();
        for (GroupTeamEntity team : teams) {
            teamSummaries.add(TeamSummary.builder()
                    .groupTeamId(team.getGroupTeamId())
                    .leaderUserId(team.getLeaderUserId())
                    .skuId(team.getSkuId())
                    .requiredNum(team.getRequiredNum())
                    .currentNum(team.getCurrentNum())
                    .settledTradeNum(team.getSettledTradeNum())
                    .teamStatus(team.getTeamStatus().getCode())
                    .teamStatusName(team.getTeamStatus().getInfo())
                    .teamCreateTime(team.getTeamCreateTime())
                    .teamExpireTime(team.getTeamExpireTime())
                    .build());
        }

        return new ActivityDetailResult(activity, totalTeams, teamSummaries);
    }

    /**
     * 队伍成员列表 —— 某队伍的所有参与记录及订单状态
     */
    public List<TeamMember> getTeamMembers(Long activityBusinessId) {
        List<ActivityOrderRecordEntity> records = repository.getActivityRecordsByBusinessId(activityBusinessId);
        List<TeamMember> members = new ArrayList<>();

        for (ActivityOrderRecordEntity record : records) {
            // 查订单状态
            String orderStatus = "未知";
            if (record.getRecordStatus() != null) {
                orderStatus = switch (record.getRecordStatus()) {
                    case 0 -> "处理中";
                    case 1 -> "已结算";
                    case 2 -> "处理失败";
                    case 3 -> "已取消";
                    default -> "未知";
                };
            }

            members.add(TeamMember.builder()
                    .orderNo(record.getOrderNo())
                    .userId(record.getUserId())
                    .participationType(record.getParticipationType() != null
                            ? record.getParticipationType().getCode() : null)
                    .participationTypeName(record.getParticipationType() != null
                            ? record.getParticipationType().getInfo() : null)
                    .orderStatus(orderStatus)
                    .joinTime(record.getJoinTime())
                    .build());
        }

        return members;
    }

    /**
     * 销售总览 —— 今日 + 昨日对比
     */
    public SalesOverviewResult getSalesOverview() {
        int todayOrders = repository.countTodayOrders();
        BigDecimal todayRevenue = repository.sumTodayRevenue();
        int yesterdayOrders = repository.countYesterdayOrders();
        BigDecimal yesterdayRevenue = repository.sumYesterdayRevenue();

        return new SalesOverviewResult(
                todayOrders,
                todayRevenue != null ? todayRevenue : BigDecimal.ZERO,
                yesterdayOrders,
                yesterdayRevenue != null ? yesterdayRevenue : BigDecimal.ZERO
        );
    }

    /**
     * 商品销售排行
     */
    public List<ProductRankingItem> getProductRanking(int limit) {
        List<OrderItemEntity> items = repository.getProductSalesRanking(limit);
        List<ProductRankingItem> result = new ArrayList<>();

        int rank = 1;
        for (OrderItemEntity item : items) {
            result.add(new ProductRankingItem(
                    rank++,
                    item.getSkuId(),
                    item.getProductName(),
                    item.getQuantity(),
                    item.getActualPrice() != null ? item.getActualPrice() : BigDecimal.ZERO
            ));
        }

        return result;
    }

}
