package cn.pumluda.infrastructure.dao;

import cn.pumluda.infrastructure.dao.po.TradeOrderPo;
import org.apache.ibatis.annotations.Mapper;

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

    List<String> queryTimeoutOrders();

}
