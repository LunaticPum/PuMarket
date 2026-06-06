package cn.pumluda.trigger.http;

import cn.pumluda.api.IMarketingController;
import cn.pumluda.api.dto.*;
import cn.pumluda.api.response.Response;
import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.service.marketing.ActivityDetailResult;
import cn.pumluda.domain.trade.service.marketing.ActivityOverview;
import cn.pumluda.domain.trade.service.marketing.MarketingQueryService;
import cn.pumluda.domain.trade.service.marketing.ProductRankingItem;
import cn.pumluda.domain.trade.service.marketing.SalesOverviewResult;
import cn.pumluda.domain.trade.service.marketing.TeamMember;
import cn.pumluda.domain.trade.service.marketing.TeamSummary;
import cn.pumluda.infrastructure.auth.MarketingAuthService;
import cn.pumluda.types.enums.ActivityTypeEnum;
import cn.pumluda.types.enums.ResponseEnum;
import cn.pumluda.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: MarketingController <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 营销后台管理 Controller
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/marketing")
public class MarketingController implements IMarketingController {

    @Resource
    private MarketingAuthService authService;

    @Resource
    private MarketingQueryService marketingQueryService;

    @PostMapping("login")
    @Override
    public Response<MarketingLoginResDTO> login(@RequestBody MarketingLoginReqDTO requestDTO) {
        try {
            if (requestDTO.getUsername() == null || requestDTO.getUsername().isBlank()
                    || requestDTO.getPassword() == null || requestDTO.getPassword().isBlank()) {
                return Response.<MarketingLoginResDTO>builder()
                        .code(ResponseEnum.ILLEGAL_PARAMETER.getCode())
                        .info("用户名和密码不能为空")
                        .build();
            }

            String token = authService.login(requestDTO.getUsername(), requestDTO.getPassword());

            return Response.<MarketingLoginResDTO>builder()
                    .code(ResponseEnum.SUCCESS.getCode())
                    .info(ResponseEnum.SUCCESS.getInfo())
                    .data(MarketingLoginResDTO.builder()
                            .token(token)
                            .username(requestDTO.getUsername())
                            .role("ADMIN")
                            .build())
                    .build();

        } catch (AppException e) {
            return Response.<MarketingLoginResDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("[营销登录] 异常", e);
            return Response.<MarketingLoginResDTO>builder()
                    .code(ResponseEnum.UN_ERROR.getCode())
                    .info(ResponseEnum.UN_ERROR.getInfo())
                    .build();
        }
    }

    @PostMapping("activity/overview")
    @Override
    public Response<ActivityOverviewResDTO> getActivityOverview() {
        try {
            List<ActivityOverview> overviews = marketingQueryService.getActivityOverview();

            List<ActivityOverviewResDTO.ActivityStat> stats = new ArrayList<>();
            for (ActivityOverview ov : overviews) {
                stats.add(ActivityOverviewResDTO.ActivityStat.builder()
                        .activityId(ov.getActivityId())
                        .activityName(ov.getActivityName())
                        .activityType(ov.getActivityType())
                        .totalTeams(ov.getTotalTeams())
                        .activeTeams(ov.getActiveTeams())
                        .completedTeams(ov.getCompletedTeams())
                        .failedTeams(ov.getFailedTeams())
                        .totalParticipants(ov.getTotalParticipants())
                        .settledOrders(ov.getSettledOrders())
                        .settlementRate(ov.getSettlementRate())
                        .remainingQuota(ov.getRemainingQuota())
                        .startTime(ov.getStartTime())
                        .endTime(ov.getEndTime())
                        .build());
            }

            return Response.<ActivityOverviewResDTO>builder()
                    .code(ResponseEnum.SUCCESS.getCode())
                    .info(ResponseEnum.SUCCESS.getInfo())
                    .data(ActivityOverviewResDTO.builder().list(stats).build())
                    .build();

        } catch (Exception e) {
            log.error("[营销后台-活动总览] 异常", e);
            return Response.<ActivityOverviewResDTO>builder()
                    .code(ResponseEnum.UN_ERROR.getCode())
                    .info(ResponseEnum.UN_ERROR.getInfo())
                    .build();
        }
    }

    @PostMapping("activity/detail")
    @Override
    public Response<ActivityDetailResDTO> getActivityDetail(@RequestBody ActivityDetailReqDTO requestDTO) {
        try {
            Long activityId = requestDTO.getActivityId();
            if (activityId == null) {
                return Response.<ActivityDetailResDTO>builder()
                        .code(ResponseEnum.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseEnum.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            int page = requestDTO.getPage() != null && requestDTO.getPage() > 0 ? requestDTO.getPage() : 1;
            int size = requestDTO.getSize() != null && requestDTO.getSize() > 0 ? requestDTO.getSize() : 20;

            ActivityDetailResult result = marketingQueryService.getActivityDetail(activityId, page, size);
            if (result == null) {
                return Response.<ActivityDetailResDTO>builder()
                        .code(ResponseEnum.ACTIVITY_NULL.getCode())
                        .info(ResponseEnum.ACTIVITY_NULL.getInfo())
                        .build();
            }

            ActivityConfigEntity activity = result.getActivity();

            List<ActivityDetailResDTO.TeamCard> teamCards = new ArrayList<>();
            for (TeamSummary ts : result.getTeams()) {
                teamCards.add(ActivityDetailResDTO.TeamCard.builder()
                        .groupTeamId(ts.getGroupTeamId())
                        .leaderUserId(ts.getLeaderUserId())
                        .skuId(ts.getSkuId())
                        .requiredNum(ts.getRequiredNum())
                        .currentNum(ts.getCurrentNum())
                        .settledTradeNum(ts.getSettledTradeNum())
                        .teamStatus(ts.getTeamStatus())
                        .teamStatusName(ts.getTeamStatusName())
                        .teamCreateTime(ts.getTeamCreateTime())
                        .teamExpireTime(ts.getTeamExpireTime())
                        .build());
            }

            return Response.<ActivityDetailResDTO>builder()
                    .code(ResponseEnum.SUCCESS.getCode())
                    .info(ResponseEnum.SUCCESS.getInfo())
                    .data(ActivityDetailResDTO.builder()
                            .activityId(activity.getActivityId())
                            .activityName(activity.getActivityName())
                            .activityType(activity.getActivityType())
                            .activityTypeName(ActivityTypeEnum.of(activity.getActivityType()).getName())
                            .discountType(activity.getDiscountType() != null
                                    ? activity.getDiscountType().getCode() : 0)
                            .totalDiscountQuota(activity.getTotalDiscountQuota())
                            .status(activity.getStatus())
                            .startTime(activity.getStartTime())
                            .endTime(activity.getEndTime())
                            .totalTeams(result.getTotalTeams())
                            .teams(teamCards)
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("[营销后台-活动详情] 异常", e);
            return Response.<ActivityDetailResDTO>builder()
                    .code(ResponseEnum.UN_ERROR.getCode())
                    .info(ResponseEnum.UN_ERROR.getInfo())
                    .build();
        }
    }

    @PostMapping("team/members")
    @Override
    public Response<TeamMembersResDTO> getTeamMembers(@RequestBody TeamMembersReqDTO requestDTO) {
        try {
            Long groupTeamId = requestDTO.getGroupTeamId();
            if (groupTeamId == null) {
                return Response.<TeamMembersResDTO>builder()
                        .code(ResponseEnum.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseEnum.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            List<TeamMember> members = marketingQueryService.getTeamMembers(groupTeamId);

            List<TeamMembersResDTO.Member> memberDTOs = new ArrayList<>();
            for (TeamMember m : members) {
                memberDTOs.add(TeamMembersResDTO.Member.builder()
                        .orderNo(m.getOrderNo())
                        .userId(m.getUserId())
                        .participationType(m.getParticipationType())
                        .participationTypeName(m.getParticipationTypeName())
                        .orderStatus(m.getOrderStatus())
                        .joinTime(m.getJoinTime())
                        .build());
            }

            return Response.<TeamMembersResDTO>builder()
                    .code(ResponseEnum.SUCCESS.getCode())
                    .info(ResponseEnum.SUCCESS.getInfo())
                    .data(TeamMembersResDTO.builder().members(memberDTOs).build())
                    .build();

        } catch (Exception e) {
            log.error("[营销后台-队伍成员] 异常", e);
            return Response.<TeamMembersResDTO>builder()
                    .code(ResponseEnum.UN_ERROR.getCode())
                    .info(ResponseEnum.UN_ERROR.getInfo())
                    .build();
        }
    }

    @PostMapping("sales/overview")
    @Override
    public Response<SalesOverviewResDTO> getSalesOverview() {
        try {
            SalesOverviewResult result = marketingQueryService.getSalesOverview();

            return Response.<SalesOverviewResDTO>builder()
                    .code(ResponseEnum.SUCCESS.getCode())
                    .info(ResponseEnum.SUCCESS.getInfo())
                    .data(SalesOverviewResDTO.builder()
                            .todayOrderCount(result.getTodayOrderCount())
                            .todayTotalAmount(result.getTodayTotalAmount())
                            .yesterdayOrderCount(result.getYesterdayOrderCount())
                            .yesterdayTotalAmount(result.getYesterdayTotalAmount())
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("[营销后台-销售总览] 异常", e);
            return Response.<SalesOverviewResDTO>builder()
                    .code(ResponseEnum.UN_ERROR.getCode())
                    .info(ResponseEnum.UN_ERROR.getInfo())
                    .build();
        }
    }

    @PostMapping("sales/ranking")
    @Override
    public Response<ProductRankingResDTO> getProductRanking(@RequestBody ProductRankingReqDTO requestDTO) {
        try {
            int limit = requestDTO.getLimit() != null && requestDTO.getLimit() > 0 ? requestDTO.getLimit() : 10;

            List<ProductRankingItem> items = marketingQueryService.getProductRanking(limit);

            List<ProductRankingResDTO.RankItem> rankItems = new ArrayList<>();
            for (ProductRankingItem item : items) {
                rankItems.add(ProductRankingResDTO.RankItem.builder()
                        .rank(item.getRank())
                        .skuId(item.getSkuId())
                        .productName(item.getProductName())
                        .soldCount(item.getSoldCount())
                        .totalAmount(item.getTotalAmount())
                        .build());
            }

            return Response.<ProductRankingResDTO>builder()
                    .code(ResponseEnum.SUCCESS.getCode())
                    .info(ResponseEnum.SUCCESS.getInfo())
                    .data(ProductRankingResDTO.builder().list(rankItems).build())
                    .build();

        } catch (Exception e) {
            log.error("[营销后台-商品排行] 异常", e);
            return Response.<ProductRankingResDTO>builder()
                    .code(ResponseEnum.UN_ERROR.getCode())
                    .info(ResponseEnum.UN_ERROR.getInfo())
                    .build();
        }
    }

}
