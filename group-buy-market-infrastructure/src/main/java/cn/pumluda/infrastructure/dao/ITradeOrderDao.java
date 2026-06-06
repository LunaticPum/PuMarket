package cn.pumluda.infrastructure.dao;

import cn.pumluda.infrastructure.dao.po.TradeOrderPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: ITradeOrderDao <p>
 * Created by: 16374 <p>
 * Date: 2026/5/30 <p>
 * Time: 16:31 <p>
 * Description: 订单主表 DAO
 */
@Mapper
public interface ITradeOrderDao {

    TradeOrderPo getOrderByOrderNo(String orderNo);

    void addTradeOrder(TradeOrderPo tradeOrder);

    List<TradeOrderPo> queryTimeoutOrders();

    void closeTradeOrder(@Param("orderNo") String orderNo, @Param("userId") Long userId);

    void settleTradeOrder(@Param("orderNo") String orderNo, @Param("userId") Long userId);

    void cancelTradeOrder(@Param("orderNo") String orderNo, @Param("userId") Long userId);

    List<TradeOrderPo> getOrdersByUserId(@Param("userId") Long userId,
                                          @Param("offset") int offset,
                                          @Param("limit") int limit);

    int countOrdersByUserId(@Param("userId") Long userId);

    // ==================== 销售统计 ====================

    int countTodayOrders();

    java.math.BigDecimal sumTodayRevenue();

    int countYesterdayOrders();

    java.math.BigDecimal sumYesterdayRevenue();
}
