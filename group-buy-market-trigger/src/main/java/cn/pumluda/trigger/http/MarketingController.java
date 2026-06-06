package cn.pumluda.trigger.http;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.pumluda.api.IMarketingController;
import cn.pumluda.api.dto.*;
import cn.pumluda.api.response.Response;
import cn.pumluda.domain.trade.adapter.repository.ITradeRepository;
import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.service.activityManage.ActivityManageService;
import cn.pumluda.domain.trade.service.marketing.*;
import cn.pumluda.infrastructure.auth.MarketingAuthService;
import cn.pumluda.types.enums.ActivityTypeEnum;
import cn.pumluda.types.enums.DiscountTypeEnum;
import cn.pumluda.types.enums.ResponseEnum;
import cn.pumluda.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 营销后台管理 Controller
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
    @Resource
    private ActivityManageService activityManageService;
    @Resource
    private ITradeRepository repository;

    private final Snowflake snowflake = IdUtil.getSnowflake(2, 1);

    // ==================== 登录 ====================

    @PostMapping("login")
    @Override
    public Response<MarketingLoginResDTO> login(@RequestBody MarketingLoginReqDTO req) {
        try {
            if (req.getUsername() == null || req.getUsername().isBlank()
                    || req.getPassword() == null || req.getPassword().isBlank()) {
                return Response.<MarketingLoginResDTO>builder()
                        .code(ResponseEnum.ILLEGAL_PARAMETER.getCode()).info("用户名和密码不能为空").build();
            }
            String token = authService.login(req.getUsername(), req.getPassword());
            return Response.<MarketingLoginResDTO>builder()
                    .code(ResponseEnum.SUCCESS.getCode()).info(ResponseEnum.SUCCESS.getInfo())
                    .data(MarketingLoginResDTO.builder().token(token).username(req.getUsername()).role("ADMIN").build())
                    .build();
        } catch (AppException e) {
            return Response.<MarketingLoginResDTO>builder().code(e.getCode()).info(e.getInfo()).build();
        } catch (Exception e) {
            log.error("[营销登录] 异常", e);
            return Response.<MarketingLoginResDTO>builder().code(ResponseEnum.UN_ERROR.getCode()).info(ResponseEnum.UN_ERROR.getInfo()).build();
        }
    }

    // ==================== 活动总览 ====================

    @PostMapping("activity/overview")
    @Override
    public Response<ActivityOverviewResDTO> getActivityOverview() {
        try {
            List<ActivityOverview> overviews = marketingQueryService.getActivityOverview();
            List<ActivityOverviewResDTO.ActivityStat> list = new ArrayList<>();
            for (ActivityOverview ov : overviews) {
                list.add(ActivityOverviewResDTO.ActivityStat.builder()
                        .activityId(ov.getActivityId()).activityName(ov.getActivityName())
                        .activityType(ov.getActivityType()).totalTeams(ov.getTotalTeams())
                        .activeTeams(ov.getActiveTeams()).completedTeams(ov.getCompletedTeams())
                        .failedTeams(ov.getFailedTeams()).totalParticipants(ov.getTotalParticipants())
                        .settledOrders(ov.getSettledOrders()).settlementRate(ov.getSettlementRate())
                        .remainingQuota(ov.getRemainingQuota()).startTime(ov.getStartTime()).endTime(ov.getEndTime())
                        .build());
            }
            return Response.<ActivityOverviewResDTO>builder()
                    .code(ResponseEnum.SUCCESS.getCode()).info(ResponseEnum.SUCCESS.getInfo())
                    .data(ActivityOverviewResDTO.builder().list(list).build()).build();
        } catch (Exception e) {
            log.error("[活动总览] 异常", e);
            return Response.<ActivityOverviewResDTO>builder().code(ResponseEnum.UN_ERROR.getCode()).info(ResponseEnum.UN_ERROR.getInfo()).build();
        }
    }

    // ==================== 活动详情 ====================

    @PostMapping("activity/detail")
    @Override
    public Response<ActivityDetailResDTO> getActivityDetail(@RequestBody ActivityDetailReqDTO req) {
        try {
            if (req.getActivityId() == null) {
                return Response.<ActivityDetailResDTO>builder().code(ResponseEnum.ILLEGAL_PARAMETER.getCode()).info(ResponseEnum.ILLEGAL_PARAMETER.getInfo()).build();
            }
            int page = req.getPage() != null && req.getPage() > 0 ? req.getPage() : 1;
            int size = req.getSize() != null && req.getSize() > 0 ? req.getSize() : 20;
            ActivityDetailResult result = marketingQueryService.getActivityDetail(req.getActivityId(), page, size);
            if (result == null) {
                return Response.<ActivityDetailResDTO>builder().code(ResponseEnum.ACTIVITY_NULL.getCode()).info(ResponseEnum.ACTIVITY_NULL.getInfo()).build();
            }
            ActivityConfigEntity act = result.getActivity();
            List<ActivityDetailResDTO.TeamCard> teams = new ArrayList<>();
            for (TeamSummary ts : result.getTeams()) {
                teams.add(ActivityDetailResDTO.TeamCard.builder()
                        .groupTeamId(ts.getGroupTeamId()).leaderUserId(ts.getLeaderUserId()).skuId(ts.getSkuId())
                        .requiredNum(ts.getRequiredNum()).currentNum(ts.getCurrentNum()).settledTradeNum(ts.getSettledTradeNum())
                        .teamStatus(ts.getTeamStatus()).teamStatusName(ts.getTeamStatusName())
                        .teamCreateTime(ts.getTeamCreateTime()).teamExpireTime(ts.getTeamExpireTime()).build());
            }
            return Response.<ActivityDetailResDTO>builder()
                    .code(ResponseEnum.SUCCESS.getCode()).info(ResponseEnum.SUCCESS.getInfo())
                    .data(ActivityDetailResDTO.builder()
                            .activityId(act.getActivityId()).activityName(act.getActivityName())
                            .activityType(act.getActivityType()).activityTypeName(ActivityTypeEnum.of(act.getActivityType()).getName())
                            .discountType(act.getDiscountType() != null ? act.getDiscountType().getCode() : 0)
                            .totalDiscountQuota(act.getTotalDiscountQuota()).status(act.getStatus())
                            .startTime(act.getStartTime()).endTime(act.getEndTime())
                            .totalTeams(result.getTotalTeams()).teams(teams).build()).build();
        } catch (Exception e) {
            log.error("[活动详情] 异常", e);
            return Response.<ActivityDetailResDTO>builder().code(ResponseEnum.UN_ERROR.getCode()).info(ResponseEnum.UN_ERROR.getInfo()).build();
        }
    }

    // ==================== 队伍成员 ====================

    @PostMapping("team/members")
    @Override
    public Response<TeamMembersResDTO> getTeamMembers(@RequestBody TeamMembersReqDTO req) {
        try {
            if (req.getGroupTeamId() == null) {
                return Response.<TeamMembersResDTO>builder().code(ResponseEnum.ILLEGAL_PARAMETER.getCode()).info(ResponseEnum.ILLEGAL_PARAMETER.getInfo()).build();
            }
            List<TeamMember> members = marketingQueryService.getTeamMembers(req.getGroupTeamId());
            List<TeamMembersResDTO.Member> list = new ArrayList<>();
            for (TeamMember m : members) {
                list.add(TeamMembersResDTO.Member.builder()
                        .orderNo(m.getOrderNo()).userId(m.getUserId()).participationType(m.getParticipationType())
                        .participationTypeName(m.getParticipationTypeName()).orderStatus(m.getOrderStatus()).joinTime(m.getJoinTime()).build());
            }
            return Response.<TeamMembersResDTO>builder()
                    .code(ResponseEnum.SUCCESS.getCode()).info(ResponseEnum.SUCCESS.getInfo())
                    .data(TeamMembersResDTO.builder().members(list).build()).build();
        } catch (Exception e) {
            log.error("[队伍成员] 异常", e);
            return Response.<TeamMembersResDTO>builder().code(ResponseEnum.UN_ERROR.getCode()).info(ResponseEnum.UN_ERROR.getInfo()).build();
        }
    }

    // ==================== 销售 ====================

    @PostMapping("sales/overview")
    @Override
    public Response<SalesOverviewResDTO> getSalesOverview() {
        try {
            SalesOverviewResult r = marketingQueryService.getSalesOverview();
            return Response.<SalesOverviewResDTO>builder()
                    .code(ResponseEnum.SUCCESS.getCode()).info(ResponseEnum.SUCCESS.getInfo())
                    .data(SalesOverviewResDTO.builder().todayOrderCount(r.getTodayOrderCount()).todayTotalAmount(r.getTodayTotalAmount())
                            .yesterdayOrderCount(r.getYesterdayOrderCount()).yesterdayTotalAmount(r.getYesterdayTotalAmount()).build()).build();
        } catch (Exception e) {
            log.error("[销售总览] 异常", e);
            return Response.<SalesOverviewResDTO>builder().code(ResponseEnum.UN_ERROR.getCode()).info(ResponseEnum.UN_ERROR.getInfo()).build();
        }
    }

    @PostMapping("sales/ranking")
    @Override
    public Response<ProductRankingResDTO> getProductRanking(@RequestBody ProductRankingReqDTO req) {
        try {
            int limit = req.getLimit() != null && req.getLimit() > 0 ? req.getLimit() : 10;
            List<ProductRankingItem> items = marketingQueryService.getProductRanking(limit);
            List<ProductRankingResDTO.RankItem> list = new ArrayList<>();
            for (ProductRankingItem item : items) {
                list.add(ProductRankingResDTO.RankItem.builder().rank(item.getRank()).skuId(item.getSkuId())
                        .productName(item.getProductName()).soldCount(item.getSoldCount()).totalAmount(item.getTotalAmount()).build());
            }
            return Response.<ProductRankingResDTO>builder()
                    .code(ResponseEnum.SUCCESS.getCode()).info(ResponseEnum.SUCCESS.getInfo())
                    .data(ProductRankingResDTO.builder().list(list).build()).build();
        } catch (Exception e) {
            log.error("[商品排行] 异常", e);
            return Response.<ProductRankingResDTO>builder().code(ResponseEnum.UN_ERROR.getCode()).info(ResponseEnum.UN_ERROR.getInfo()).build();
        }
    }

    // ==================== 活动管理 ====================

    @PostMapping("activity/create")
    @Override
    public Response<?> createActivity(@RequestBody CreateActivityReqDTO req) {
        try {
            ActivityConfigEntity entity = new ActivityConfigEntity();
            entity.setActivityId(snowflake.nextId());
            entity.setActivityName(req.getActivityName());
            entity.setActivityType(req.getActivityType());
            entity.setDiscountType(DiscountTypeEnum.of(req.getDiscountType()));
            entity.setDiscountConfig(JSON.parseObject(req.getDiscountParam(), Map.class));
            entity.setTotalDiscountQuota(req.getTotalQuota());
            entity.setUsedDiscountQuota(0);
            entity.setLimitTag(0);
            entity.setStatus(1);
            entity.setStartTime(req.getStartTime());
            entity.setEndTime(req.getEndTime());

            activityManageService.createActivity(entity, req.getSkuIds());
            return Response.<Map<String, Object>>builder()
                    .code(ResponseEnum.SUCCESS.getCode()).info("活动创建成功")
                    .data(Map.of("activityId", entity.getActivityId())).build();
        } catch (AppException e) {
            return Response.<String>builder().code(e.getCode()).info(e.getInfo()).build();
        } catch (Exception e) {
            log.error("[创建活动] 异常", e);
            return Response.<String>builder().code(ResponseEnum.UN_ERROR.getCode()).info("创建失败: " + e.getMessage()).build();
        }
    }

    @PostMapping("activity/update")
    @Override
    public Response<?> updateActivity(@RequestBody UpdateActivityReqDTO req) {
        try {
            ActivityConfigEntity entity = new ActivityConfigEntity();
            entity.setActivityId(req.getActivityId());
            entity.setActivityName(req.getActivityName());
            entity.setDiscountType(DiscountTypeEnum.of(req.getDiscountType()));
            entity.setDiscountConfig(JSON.parseObject(req.getDiscountParam(), Map.class));
            entity.setTotalDiscountQuota(req.getTotalQuota());
            entity.setStartTime(req.getStartTime());
            entity.setEndTime(req.getEndTime());

            int completed = activityManageService.updateActivity(entity);
            return Response.<Map<String, Object>>builder()
                    .code(ResponseEnum.SUCCESS.getCode()).info("活动已更新，" + completed + "个队伍已强制成团")
                    .data(Map.of("completedTeams", completed)).build();
        } catch (Exception e) {
            log.error("[更新活动] 异常", e);
            return Response.<String>builder().code(ResponseEnum.UN_ERROR.getCode()).info(e.getMessage()).build();
        }
    }

    @PostMapping("activity/revoke")
    @Override
    public Response<?> revokeActivity(@RequestBody RevokeActivityReqDTO req) {
        try {
            int completed = activityManageService.revokeActivity(req.getActivityId());
            return Response.<Map<String, Object>>builder()
                    .code(ResponseEnum.SUCCESS.getCode()).info("活动已撤销")
                    .data(Map.of("completedTeams", completed)).build();
        } catch (Exception e) {
            log.error("[撤销活动] 异常", e);
            return Response.<String>builder().code(ResponseEnum.UN_ERROR.getCode()).info(e.getMessage()).build();
        }
    }

    @PostMapping("activity/published")
    @Override
    public Response<PublishedActivityResDTO> getPublishedActivities() {
        try {
            List<ActivityConfigEntity> activities = activityManageService.getPublishedActivities();
            List<PublishedActivityResDTO.PubActivity> list = new ArrayList<>();
            for (ActivityConfigEntity a : activities) {
                List<Long> skuIds = repository.findSkuIdsByActivityId(a.getActivityId());
                int teamCount = repository.countTeamsByActivityId(a.getActivityId());
                list.add(PublishedActivityResDTO.PubActivity.builder()
                        .activityId(a.getActivityId()).activityName(a.getActivityName())
                        .activityType(a.getActivityType())
                        .discountType(a.getDiscountType() != null ? a.getDiscountType().getCode() : 0)
                        .startTime(a.getStartTime()).endTime(a.getEndTime())
                        .status(a.getStatus()).teamCount(teamCount).skuIds(skuIds).build());
            }
            return Response.<PublishedActivityResDTO>builder()
                    .code(ResponseEnum.SUCCESS.getCode()).info(ResponseEnum.SUCCESS.getInfo())
                    .data(PublishedActivityResDTO.builder().list(list).build()).build();
        } catch (Exception e) {
            log.error("[已发布活动] 异常", e);
            return Response.<PublishedActivityResDTO>builder().code(ResponseEnum.UN_ERROR.getCode()).info(ResponseEnum.UN_ERROR.getInfo()).build();
        }
    }

    // ==================== 首页数据 ====================

    @PostMapping("dashboard/overview")
    @Override
    public Response<DashboardOverviewResDTO> getDashboardOverview() {
        try {
            SalesOverviewResult sales = marketingQueryService.getSalesOverview();
            List<ActivityConfigEntity> activities = activityManageService.getPublishedActivities();
            List<ProductRankingItem> ranking = marketingQueryService.getProductRanking(5);

            List<ProductRankingResDTO.RankItem> topProducts = new ArrayList<>();
            for (ProductRankingItem item : ranking) {
                topProducts.add(ProductRankingResDTO.RankItem.builder()
                        .rank(item.getRank()).skuId(item.getSkuId()).productName(item.getProductName())
                        .soldCount(item.getSoldCount()).totalAmount(item.getTotalAmount()).build());
            }

            List<PublishedActivityResDTO.PubActivity> activeActs = new ArrayList<>();
            for (ActivityConfigEntity a : activities) {
                List<Long> skuIds = repository.findSkuIdsByActivityId(a.getActivityId());
                int teamCount = repository.countTeamsByActivityId(a.getActivityId());
                activeActs.add(PublishedActivityResDTO.PubActivity.builder()
                        .activityId(a.getActivityId()).activityName(a.getActivityName())
                        .activityType(a.getActivityType())
                        .discountType(a.getDiscountType() != null ? a.getDiscountType().getCode() : 0)
                        .startTime(a.getStartTime()).endTime(a.getEndTime())
                        .status(a.getStatus()).teamCount(teamCount).skuIds(skuIds).build());
            }

            DashboardOverviewResDTO dto = DashboardOverviewResDTO.builder()
                    .todayOrders(sales.getTodayOrderCount()).todayRevenue(sales.getTodayTotalAmount())
                    .activeActivityCount(activities.size()).pendingShipTeams(0)
                    .blacklist(List.of()).topProducts(topProducts).activeActivities(activeActs).build();

            return Response.<DashboardOverviewResDTO>builder()
                    .code(ResponseEnum.SUCCESS.getCode()).info(ResponseEnum.SUCCESS.getInfo()).data(dto).build();
        } catch (Exception e) {
            log.error("[首页数据] 异常", e);
            return Response.<DashboardOverviewResDTO>builder().code(ResponseEnum.UN_ERROR.getCode()).info(ResponseEnum.UN_ERROR.getInfo()).build();
        }
    }

    @PostMapping("dashboard/sales")
    @Override
    public Response<SalesDetailResDTO> getSalesDetail(@RequestBody SalesDetailReqDTO req) {
        try {
            SalesOverviewResult sales = marketingQueryService.getSalesOverview();
            return Response.<SalesDetailResDTO>builder()
                    .code(ResponseEnum.SUCCESS.getCode()).info(ResponseEnum.SUCCESS.getInfo())
                    .data(SalesDetailResDTO.builder().period(req.getPeriod())
                            .totalOrders(sales.getTodayOrderCount()).totalRevenue(sales.getTodayTotalAmount())
                            .cancelCount(0).items(List.of()).build()).build();
        } catch (Exception e) {
            log.error("[销售明细] 异常", e);
            return Response.<SalesDetailResDTO>builder().code(ResponseEnum.UN_ERROR.getCode()).info(ResponseEnum.UN_ERROR.getInfo()).build();
        }
    }

}
