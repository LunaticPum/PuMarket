package cn.pumluda.domain.trade.service.lifecycle;

import cn.pumluda.domain.trade.model.entity.OrderItemEntity;

/**
 * Project: group-buy-market-better <p>
 * File: ITradeLifecycleService <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 交易生命周期服务 —— 订单结算、订单取消
 */
public interface ITradeLifecycleService {

    /**
     * 结算订单（支付完成回调）
     *
     * @param orderNo 交易单号（由前端生成）
     * @param userId  用户 ID
     * @return 更新后的订单明细
     * @throws cn.pumluda.types.exception.AppException 业务异常
     */
    OrderItemEntity settleOrder(String orderNo, Long userId);

    /**
     * 取消订单
     *
     * @param orderNo 交易单号（由前端生成）
     * @param userId  用户 ID
     * @return 更新后的订单明细
     * @throws cn.pumluda.types.exception.AppException 业务异常
     */
    OrderItemEntity cancelOrder(String orderNo, Long userId);

}
