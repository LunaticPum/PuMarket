package cn.pumluda.domain.trade.service.userQuery;

import cn.pumluda.domain.trade.adapter.repository.ITradeRepository;
import cn.pumluda.domain.trade.model.aggregate.OrderAggregate;
import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.GroupTeamEntity;
import cn.pumluda.domain.trade.model.entity.OrderItemEntity;
import cn.pumluda.types.common.ActivityConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: UserOrderQueryService <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 用户订单查询领域服务 —— 聚合订单+明细+队伍，计算展示状态
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserOrderQueryService {

    private final ITradeRepository repository;

    /**
     * 分页查询用户订单列表（含展示状态计算）
     *
     * @param userId 用户 ID
     * @param page   页码（从1开始）
     * @param size   每页条数
     * @return 用户订单聚合结果
     */
    public UserOrderListResult getUserOrderList(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        log.info("[用户订单] 查询用户订单列表 userId={} page={} size={}", userId, page, size);

        List<OrderAggregate> orders = repository.getOrdersByUserId(userId, offset, size);
        int total = repository.countOrdersByUserId(userId);

        List<UserOrderItem> items = new ArrayList<>();
        for (OrderAggregate order : orders) {
            OrderItemEntity orderItem = repository.getOrderItemByOrderNo(order.getOrderNo());
            if (orderItem == null) {
                log.warn("[用户订单] 订单明细缺失，跳过 orderNo={}", order.getOrderNo());
                continue;
            }

            GroupTeamEntity team = null;
            ActivityConfigEntity activity = null;
            if (orderItem.getActivityId() != null
                    && orderItem.getActivityId() > ActivityConstants.EMPTY_ACTIVITY
                    && orderItem.getActivityBusinessId() != null) {
                team = repository.getGroupTeam(orderItem.getActivityId(), orderItem.getActivityBusinessId());
                activity = repository.getActivityByActivityId(orderItem.getActivityId());
            }

            String displayStatus = computeDisplayStatus(order, team);

            items.add(UserOrderItem.builder()
                    .orderNo(order.getOrderNo())
                    .skuId(orderItem.getSkuId())
                    .productName(orderItem.getProductName())
                    .originPrice(orderItem.getOriginPrice())
                    .actualPrice(orderItem.getActualPrice())
                    .quantity(orderItem.getQuantity())
                    .orderStatus(order.getOrderStatus().getCode())
                    .displayStatus(displayStatus)
                    .activityName(activity != null ? activity.getActivityName() : null)
                    .groupTeamId(team != null ? team.getGroupTeamId() : null)
                    .teamStatus(team != null ? team.getTeamStatus().getCode() : null)
                    .teamProgress(team != null
                            ? team.getSettledTradeNum() + "/" + team.getRequiredNum() : null)
                    .orderCreateTime(order.getOrderCreateTime())
                    .build());
        }

        return new UserOrderListResult(total, page, size, items);
    }

    /**
     * 查询用户订单详情
     */
    public UserOrderDetailResult getUserOrderDetail(String orderNo, Long userId) {
        log.info("[用户订单] 查询用户订单详情 orderNo={} userId={}", orderNo, userId);

        OrderAggregate order = repository.getOrderByOrderNo(orderNo);
        if (order == null) {
            return null;
        }

        // 归属校验
        if (order.getUserTagRecord() != null
                && !order.getUserTagRecord().getUserId().equals(userId)) {
            log.warn("[用户订单] 订单不属于该用户 orderNo={} userId={}", orderNo, userId);
            return null;
        }

        OrderItemEntity orderItem = repository.getOrderItemByOrderNo(orderNo);
        if (orderItem == null) {
            log.warn("[用户订单] 订单明细缺失 orderNo={}", orderNo);
            return null;
        }

        GroupTeamEntity team = null;
        ActivityConfigEntity activity = null;
        if (orderItem.getActivityId() != null
                && orderItem.getActivityId() > ActivityConstants.EMPTY_ACTIVITY
                && orderItem.getActivityBusinessId() != null) {
            team = repository.getGroupTeam(orderItem.getActivityId(), orderItem.getActivityBusinessId());
            activity = repository.getActivityByActivityId(orderItem.getActivityId());
        }

        String displayStatus = computeDisplayStatus(order, team);

        UserOrderDetailResult.TeamInfo teamInfo = null;
        if (team != null) {
            teamInfo = UserOrderDetailResult.TeamInfo.builder()
                    .groupTeamId(team.getGroupTeamId())
                    .leaderUserId(team.getLeaderUserId())
                    .requiredNum(team.getRequiredNum())
                    .currentNum(team.getCurrentNum())
                    .settledTradeNum(team.getSettledTradeNum())
                    .teamStatus(team.getTeamStatus().getCode())
                    .teamStatusName(team.getTeamStatus().getInfo())
                    .remainingSlots(team.getRequiredNum() - team.getCurrentNum())
                    .teamCreateTime(team.getTeamCreateTime())
                    .teamExpireTime(team.getTeamExpireTime())
                    .build();
        }

        return UserOrderDetailResult.builder()
                .orderNo(order.getOrderNo())
                .userId(order.getUserTagRecord() != null ? order.getUserTagRecord().getUserId() : null)
                .skuId(orderItem.getSkuId())
                .productName(orderItem.getProductName())
                .originPrice(orderItem.getOriginPrice())
                .actualPrice(orderItem.getActualPrice())
                .discountPrice(orderItem.getDiscountPrice())
                .quantity(orderItem.getQuantity())
                .orderStatus(order.getOrderStatus().getCode())
                .displayStatus(displayStatus)
                .orderCreateTime(order.getOrderCreateTime())
                .orderExpireTime(order.getOrderExpireTime())
                .activityId(orderItem.getActivityId())
                .activityName(activity != null ? activity.getActivityName() : null)
                .activityType(orderItem.getActivityType())
                .teamInfo(teamInfo)
                .build();
    }

    // ==================== 私有方法 ====================

    /**
     * 根据订单状态和队伍状态交叉计算前端展示状态
     */
    private String computeDisplayStatus(OrderAggregate order, GroupTeamEntity team) {
        int orderStatus = order.getOrderStatus().getCode();

        return switch (orderStatus) {
            case 0 -> "待支付";   // CREATE — 订单已创建，等待支付
            case 2 -> "已取消";   // CLOSE — 订单已关闭
            case 1 -> {           // COMPLETE — 已支付
                if (team == null) {
                    yield "已发货";  // 单独购买，支付即发货
                }
                int teamStatus = team.getTeamStatus().getCode();
                yield switch (teamStatus) {
                    case 0 -> "待成团";    // PROGRESS — 拼团进行中
                    case 1 -> "已发货";    // COMPLETE — 成团 = 自动发货
                    case 2 -> "拼团失败";   // FAILED
                    default -> "未知";
                };
            }
            default -> "未知";
        };
    }

}
