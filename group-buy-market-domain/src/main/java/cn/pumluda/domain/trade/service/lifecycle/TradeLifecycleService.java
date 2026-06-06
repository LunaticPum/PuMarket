package cn.pumluda.domain.trade.service.lifecycle;

import cn.pumluda.domain.trade.adapter.repository.ITradeRepository;
import cn.pumluda.domain.trade.model.aggregate.OrderAggregate;
import cn.pumluda.domain.trade.model.entity.GroupTeamEntity;
import cn.pumluda.domain.trade.model.entity.OrderItemEntity;
import cn.pumluda.domain.trade.model.valobj.GroupTeamStatusEnumVo;
import cn.pumluda.domain.trade.model.valobj.OrderStatusEnumVo;
import cn.pumluda.types.common.ActivityConstants;
import cn.pumluda.types.common.RedisConstants;
import cn.pumluda.types.enums.ResponseEnum;
import cn.pumluda.types.exception.AppException;
import cn.pumluda.types.utils.RedisIdempotencyChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Project: group-buy-market-better <p>
 * File: TradeLifecycleService <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 交易生命周期服务实现 —— 订单结算和订单取消
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeLifecycleService implements ITradeLifecycleService {

    private final ITradeRepository repository;
    private final RedisIdempotencyChecker idempotencyChecker;

    /**
     * 结算订单（支付完成回调）
     * <p>
     * 业务流程：
     * 1. 幂等校验（防止重复结算）
     * 2. 订单存在性校验 + 状态校验 + 归属校验
     * 3. 事务更新：订单状态 → 已完成、消费锁定库存、拼团进度更新
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public OrderItemEntity settleOrder(String orderNo, Long userId) {
        // ========== 1. 幂等校验 ==========
        String bizId = orderNo;
        boolean firstRequest = idempotencyChecker.tryAcquire(
                RedisConstants.SETTLE_ORDER, bizId, 30
        );

        if (!firstRequest) {
            log.warn("[订单结算] 重复结算请求 orderNo={}, userId={}", orderNo, userId);
            throw new AppException(
                    ResponseEnum.DUPLICATE_CREATE_ORDER_REQUEST.getCode(),
                    "订单结算处理中，请稍后重试"
            );
        }

        try {
            // ========== 2. 订单校验 ==========
            OrderAggregate order = repository.getOrderByOrderNo(orderNo);
            if (order == null) {
                log.error("[订单结算] 订单不存在 orderNo={}", orderNo);
                throw new AppException(
                        ResponseEnum.ORDER_NOT_FOUND.getCode(),
                        ResponseEnum.ORDER_NOT_FOUND.getInfo()
                );
            }

            if (!OrderStatusEnumVo.CREATE.equals(order.getOrderStatus())) {
                log.error("[订单结算] 订单状态异常，无法结算 orderNo={}, status={}", orderNo, order.getOrderStatus());
                throw new AppException(
                        ResponseEnum.ORDER_STATUS_INVALID.getCode(),
                        ResponseEnum.ORDER_STATUS_INVALID.getInfo()
                );
            }

            // 订单归属校验：结算用户与下单用户一致
            if (order.getUserTagRecord() != null
                    && !order.getUserTagRecord().getUserId().equals(userId)) {
                log.error("[订单结算] 用户不匹配 orderNo={}, expected={}, actual={}",
                        orderNo, order.getUserTagRecord().getUserId(), userId);
                throw new AppException(
                        ResponseEnum.ILLEGAL_PARAMETER.getCode(),
                        "用户与订单不匹配"
                );
            }

            // ========== 3. 查询订单明细 ==========
            OrderItemEntity orderItem = repository.getOrderItemByOrderNo(orderNo);
            if (orderItem == null) {
                log.error("[订单结算] 订单明细不存在 orderNo={}", orderNo);
                throw new AppException(
                        ResponseEnum.UN_ERROR.getCode(),
                        "订单明细缺失"
                );
            }

            Long activityId = orderItem.getActivityId();
            Long activityBusinessId = orderItem.getActivityBusinessId();
            Long skuId = orderItem.getSkuId();
            int quantity = orderItem.getQuantity();

            log.info(
                    "[订单结算] ========== 开始结算 ==========\n" +
                    "[订单结算] orderNo={} userId={} skuId={} quantity={} activityId={} activityBusinessId={}",
                    orderNo, userId, skuId, quantity, activityId, activityBusinessId
            );

            // ========== 4. 事务操作 ==========

            // 4a. 更新订单状态
            repository.settleTradeOrder(orderNo, userId);

            // 4b. 消费锁定库存（锁定库存 → 实际扣减）
            repository.consumeLockedStock(skuId, quantity);

            // 4c. 拼团活动相关处理
            if (activityId != null && activityId > ActivityConstants.EMPTY_ACTIVITY
                    && activityBusinessId != null) {
                // 增加团内结算交易量
                repository.addSettledNum(activityId, activityBusinessId);

                // 检查团队是否已完成（结算后达到成团条件）
                GroupTeamEntity groupTeam = repository.getGroupTeam(activityId, activityBusinessId);
                if (groupTeam != null
                        && groupTeam.getSettledTradeNum() != null
                        && groupTeam.getRequiredNum() != null
                        && groupTeam.getSettledTradeNum() >= groupTeam.getRequiredNum()
                        && GroupTeamStatusEnumVo.PROGRESS.equals(groupTeam.getTeamStatus())) {
                    log.info("[订单结算] 拼团完成 activityId={} groupTeamId={}", activityId, activityBusinessId);
                    repository.completeGroupTeam(activityId, activityBusinessId);
                }

                // 结算活动参与记录
                repository.settleActivityOrderRecord(orderNo, activityId);
            }

            log.info("[订单结算] ========== 结算完成 orderNo={} ==========", orderNo);
            return orderItem;

        } catch (AppException e) {
            // 业务异常：释放幂等锁，允许用户重试
            idempotencyChecker.release(RedisConstants.SETTLE_ORDER, bizId);
            throw e;
        } catch (Exception e) {
            // 未知异常：释放幂等锁，允许用户重试
            log.error("[订单结算] 未知异常 orderNo={}", orderNo, e);
            idempotencyChecker.release(RedisConstants.SETTLE_ORDER, bizId);
            throw new AppException(
                    ResponseEnum.WRITE_DB_ERROR.getCode(),
                    ResponseEnum.WRITE_DB_ERROR.getInfo()
            );
        }
    }

    /**
     * 取消订单
     * <p>
     * 业务流程：
     * 1. 幂等校验（防止重复取消）
     * 2. 订单存在性校验 + 状态校验 + 归属校验
     * 3. 事务更新：订单状态 → 已关闭、归还锁定库存、拼团名额回退
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public OrderItemEntity cancelOrder(String orderNo, Long userId) {
        // ========== 1. 幂等校验 ==========
        String bizId = orderNo;
        boolean firstRequest = idempotencyChecker.tryAcquire(
                RedisConstants.CANCEL_ORDER, bizId, 30
        );

        if (!firstRequest) {
            log.warn("[订单取消] 重复取消请求 orderNo={}, userId={}", orderNo, userId);
            throw new AppException(
                    ResponseEnum.DUPLICATE_CREATE_ORDER_REQUEST.getCode(),
                    "订单取消处理中，请稍后重试"
            );
        }

        try {
            // ========== 2. 订单校验 ==========
            OrderAggregate order = repository.getOrderByOrderNo(orderNo);
            if (order == null) {
                log.error("[订单取消] 订单不存在 orderNo={}", orderNo);
                throw new AppException(
                        ResponseEnum.ORDER_NOT_FOUND.getCode(),
                        ResponseEnum.ORDER_NOT_FOUND.getInfo()
                );
            }

            if (!OrderStatusEnumVo.CREATE.equals(order.getOrderStatus())) {
                log.error("[订单取消] 订单状态异常，无法取消 orderNo={}, status={}", orderNo, order.getOrderStatus());
                throw new AppException(
                        ResponseEnum.ORDER_STATUS_INVALID.getCode(),
                        ResponseEnum.ORDER_STATUS_INVALID.getInfo()
                );
            }

            // 订单归属校验：取消用户与下单用户一致
            if (order.getUserTagRecord() != null
                    && !order.getUserTagRecord().getUserId().equals(userId)) {
                log.error("[订单取消] 用户不匹配 orderNo={}, expected={}, actual={}",
                        orderNo, order.getUserTagRecord().getUserId(), userId);
                throw new AppException(
                        ResponseEnum.ILLEGAL_PARAMETER.getCode(),
                        "用户与订单不匹配"
                );
            }

            // ========== 3. 查询订单明细 ==========
            OrderItemEntity orderItem = repository.getOrderItemByOrderNo(orderNo);
            if (orderItem == null) {
                log.error("[订单取消] 订单明细不存在 orderNo={}", orderNo);
                throw new AppException(
                        ResponseEnum.UN_ERROR.getCode(),
                        "订单明细缺失"
                );
            }

            Long activityId = orderItem.getActivityId();
            Long activityBusinessId = orderItem.getActivityBusinessId();
            Long skuId = orderItem.getSkuId();
            int quantity = orderItem.getQuantity();

            log.info(
                    "[订单取消] ========== 开始取消 ==========\n" +
                    "[订单取消] orderNo={} userId={} skuId={} quantity={} activityId={} activityBusinessId={}",
                    orderNo, userId, skuId, quantity, activityId, activityBusinessId
            );

            // ========== 4. 事务操作 ==========

            // 4a. 更新订单状态
            repository.cancelTradeOrder(orderNo, userId);

            // 4b. 归还库存（锁定库存 → 可用库存）
            repository.restoreSkuStock(skuId, quantity);

            // 4c. 拼团活动相关处理
            if (activityId != null && activityId > ActivityConstants.EMPTY_ACTIVITY
                    && activityBusinessId != null) {
                // 回退拼团名额
                repository.repairGroupTeamQuota(activityId, activityBusinessId);

                // 如果取消者是团长，则关闭整个拼团队伍
                GroupTeamEntity groupTeam = repository.getGroupTeam(activityId, activityBusinessId);
                if (groupTeam != null
                        && groupTeam.getLeaderUserId() != null
                        && groupTeam.getLeaderUserId().equals(userId)) {
                    log.info("[订单取消] 团长取消订单，关闭拼团队伍 activityId={} groupTeamId={}",
                            activityId, activityBusinessId);
                    repository.closeGroupTeam(activityId, activityBusinessId);
                }

                // 关闭活动参与记录
                repository.closeActivityOrderRecord(orderNo, activityId);
            }

            log.info("[订单取消] ========== 取消完成 orderNo={} ==========", orderNo);
            return orderItem;

        } catch (AppException e) {
            // 业务异常：释放幂等锁，允许用户重试
            idempotencyChecker.release(RedisConstants.CANCEL_ORDER, bizId);
            throw e;
        } catch (Exception e) {
            // 未知异常：释放幂等锁，允许用户重试
            log.error("[订单取消] 未知异常 orderNo={}", orderNo, e);
            idempotencyChecker.release(RedisConstants.CANCEL_ORDER, bizId);
            throw new AppException(
                    ResponseEnum.WRITE_DB_ERROR.getCode(),
                    ResponseEnum.WRITE_DB_ERROR.getInfo()
            );
        }
    }

}
