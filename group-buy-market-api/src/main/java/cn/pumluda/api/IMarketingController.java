package cn.pumluda.api;

import cn.pumluda.api.dto.*;
import cn.pumluda.api.response.Response;

/**
 * Project: group-buy-market-better <p>
 * File: IMarketingController <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 营销后台管理 API
 */
public interface IMarketingController {

    /**
     * 营销人员登录
     */
    Response<MarketingLoginResDTO> login(MarketingLoginReqDTO requestDTO);

    /**
     * 活动总览
     */
    Response<ActivityOverviewResDTO> getActivityOverview();

    /**
     * 活动详情（含分页队伍列表）
     */
    Response<ActivityDetailResDTO> getActivityDetail(ActivityDetailReqDTO requestDTO);

    /**
     * 队伍成员列表
     */
    Response<TeamMembersResDTO> getTeamMembers(TeamMembersReqDTO requestDTO);

    /**
     * 销售总览
     */
    Response<SalesOverviewResDTO> getSalesOverview();

    /**
     * 商品销售排行
     */
    Response<ProductRankingResDTO> getProductRanking(ProductRankingReqDTO requestDTO);

    /**
     * 创建活动
     */
    Response<?> createActivity(CreateActivityReqDTO requestDTO);

    /**
     * 撤销活动
     */
    Response<?> revokeActivity(RevokeActivityReqDTO requestDTO);

    /**
     * 已发布活动列表
     */
    Response<PublishedActivityResDTO> getPublishedActivities();

    /**
     * 营销首页数据
     */
    Response<DashboardOverviewResDTO> getDashboardOverview();

    /**
     * 商品销售明细
     */
    Response<SalesDetailResDTO> getSalesDetail(SalesDetailReqDTO requestDTO);

    /**
     * 更新活动（编辑后强制成团）
     */
    Response<?> updateActivity(UpdateActivityReqDTO requestDTO);

}
