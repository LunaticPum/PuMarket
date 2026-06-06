package cn.pumluda.domain.trade.adapter.repository;

import cn.pumluda.domain.trade.model.aggregate.OrderAggregate;
import cn.pumluda.domain.trade.model.entity.*;

import java.math.BigDecimal;
import java.util.Date;
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

    /* 分页查询上架商品列表 */
    List<SkuEntity> listSkus(int offset, int limit);

    /* 统计上架商品总数 */
    int countSkus();

    /* 按活动 ID 查找活动记录：获取指定活动配置 */
    ActivityConfigEntity getActivityByActivityId(Long activityId);

    /* 按用户 ID 查找用户记录：获取指定用户的标签信息 */
    UserTagRecordEntity getUserTagRecordById(Long userId);

    /* 按标签查找用户：统计指定标签下的用户人群 */
    List<UserTagRecordEntity> getUserTagRecordByTag(Integer userTag);

    /* 按活动 ID 和 拼团队伍 ID 查找拼团队伍 */
    GroupTeamEntity getGroupTeam(Long activityId, Long groupTeamId);

    /* 按团长用户 ID 查找拼团队伍 */
    GroupTeamEntity getGroupTeamByLeaderUserId(Long leaderUserId);

    /* 按活动 ID 查找拼团队伍：统计指定活动的拼团情况 */
    List<GroupTeamEntity> getGroupTeamByActivityId(Long activityId);

    /* 按商品 SKU ID 查找进行中的拼团队伍列表 */
    List<GroupTeamEntity> getActiveTeamsBySkuId(Long skuId);

    /* 统计某商品下进行中的拼团队伍数 */
    int countActiveTeamsBySkuId(Long skuId);

    /* 统计某商品已售数量 */
    int getSoldCountBySkuId(Long skuId);

    /* 获取所有启用且未过期的活动配置 */
    List<ActivityConfigEntity> getAllActiveActivities();

    /* 按用户 ID 查找参与活动的订单记录 */
    ActivityOrderRecordEntity getActivityOrderRecord(String orderNo, Long activityId);

    /* 按交易单号查找交易订单 */
    OrderAggregate getOrderByOrderNo(String orderNo);

    /* 按交易单号查找订单明细 */
    OrderItemEntity getOrderItemByOrderNo(String orderNo);

    /* 新增拼团队伍 */
    void addGroupTeam(GroupTeamEntity groupTeam);

    /* 更新拼团队伍信息：新增参团人员 */
    void joinGroupTeam(Long activityId, Long groupTeamId);

    /* 完成拼团队伍（结算后达到成团条件） */
    void completeGroupTeam(Long activityId, Long groupTeamId);

    /* 新增交易订单的参与活动记录 */
    void addActivityOrderRecord(ActivityOrderRecordEntity activityOrderRecord);

    /* 预占库存 */
    void reserveSkuStock(Long skuId, int quantity);

    /* 新增订单明细 */
    void addOrderItem(OrderItemEntity orderItem);

    /* 根据订单明细更新订单主表记录 */
    void addTradeOrder(OrderAggregate order);

    // ==================== 订单生命周期操作 ====================

    /* 结算订单：更新状态为已完成 */
    void settleTradeOrder(String orderNo, Long userId);

    /* 取消订单：更新状态为已关闭 */
    void cancelTradeOrder(String orderNo, Long userId);

    /* 消费锁定库存（结算后实际扣减） */
    void consumeLockedStock(Long skuId, int quantity);

    /* 归还库存（取消后返回可用库存） */
    void restoreSkuStock(Long skuId, int quantity);

    /* 增加团内结算交易量 */
    void addSettledNum(Long activityId, Long groupTeamId);

    /* 回退拼团名额（取消时归还） */
    void repairGroupTeamQuota(Long activityId, Long groupTeamId);

    /* 关闭拼团队伍 */
    void closeGroupTeam(Long activityId, Long groupTeamId);

    /* 结算活动参与记录 */
    void settleActivityOrderRecord(String orderNo, Long activityId);

    /* 关闭活动参与记录 */
    void closeActivityOrderRecord(String orderNo, Long activityId);

    // ==================== 用户订单查询 ====================

    /* 按用户 ID 分页查询订单列表 */
    List<OrderAggregate> getOrdersByUserId(Long userId, int offset, int limit);

    /* 统计用户订单总数 */
    int countOrdersByUserId(Long userId);

    // ==================== 营销数据查询 ====================

    /* 按活动 ID 分页查询拼团队伍 */
    List<GroupTeamEntity> getTeamsByActivityId(Long activityId, int offset, int limit);

    /* 统计某活动下的队伍总数 */
    int countTeamsByActivityId(Long activityId);

    /* 按业务 ID（拼团队伍 ID）查询活动参与记录（队伍成员） */
    List<ActivityOrderRecordEntity> getActivityRecordsByBusinessId(Long activityBusinessId);

    // ==================== 活动-商品关联 ====================

    void insertActivityProduct(Long activityId, Long skuId);
    void deleteActivityProductByActivityId(Long activityId);
    List<Long> findSkuIdsByActivityId(Long activityId);
    Long findActiveActivityIdBySkuId(Long skuId);
    /** 冲突检测：返回与指定时间重叠的活动-商品关联的 activityId 列表 */
    List<Long> findConflictingActivityIds(Long skuId, Date startTime, Date endTime);

    // ==================== 销售数据统计 ====================

    int countTodayOrders();
    java.math.BigDecimal sumTodayRevenue();
    int countYesterdayOrders();
    java.math.BigDecimal sumYesterdayRevenue();
    List<OrderItemEntity> getProductSalesRanking(int limit);
}
