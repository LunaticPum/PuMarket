package cn.pumluda.domain.trade.adapter.repository;

import cn.pumluda.domain.trade.model.aggregate.OrderAggregate;
import cn.pumluda.domain.trade.model.entity.*;

import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: ITradeRepository <p>
 * Created by: 16374 <p>
 * Date: 2026/5/28 <p>
 * Time: 09:17 <p>
 * Description: 交易业务仓储适配接口
 */
public interface ITradeRepository {

    /* 按 SKU ID 获取商品记录：获取指定商品 SKU 信息 */
    SkuEntity getSkuById(Long skuId);

    /* 按活动 ID 查找活动记录：获取指定活动配置 */
    ActivityConfigEntity getActivityByActivityId(Long activityId);

    /* 按用户 ID 查找用户记录：获取指定用户的标签信息 */
    UserTagRecordEntity getUserTagRecordById(Long userId);

    /* 按标签查找用户：统计指定标签下的用户人群 */
    List<UserTagRecordEntity> getUserTagRecordByTag(Integer userTag);

    /* 按拼团队伍 ID 查找拼团队伍 */
    GroupTeamEntity getGroupTeamByTeamId(Long groupTeamId);

    /* 按团长用户 ID 查找拼团队伍 */
    GroupTeamEntity getGroupTeamByLeaderUserId(Long leaderUserId);

    /* 按活动 ID 查找拼团队伍：统计指定活动的拼团情况 */
    List<GroupTeamEntity> getGroupTeamByActivityId(Long activityId);

    /* 按交易单号和活动 ID 查找交易订单的参与活动记录 */
    ActivityOrderRecordEntity getActivityOrderRecord(String orderNo, Long activityId);

    /* 按交易单号查找交易订单 */
    OrderAggregate getOrderByOrderNo(String orderNo);

    /* 新增拼团队伍 */
    void addGroupTeam(GroupTeamEntity groupTeam);

    /* 新增交易订单的参与活动记录 */
    void addActivityOrderRecord(ActivityOrderRecordEntity activityOrderRecord);

    /* 预占库存 */
    void reserveSkuStock(Long skuId, int quantity);

    /* 新增订单明细 */
    void addOrderItem(OrderItemEntity orderItem);

    /* 根据订单明细更新订单主表记录 */
    void addTradeOrder(OrderAggregate order);
}
