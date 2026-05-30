package cn.pumluda.infrastructure.dao;

import cn.pumluda.infrastructure.dao.po.TradeOrderItemPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * Project: group-buy-market-better <p>
 * File: ITradeOrderItemDao <p>
 * Created by: 16374 <p>
 * Date: 2026/5/30 <p>
 * Time: 16:30 <p>
 * Description: 订单明细 DAO
 */
@Mapper
public interface ITradeOrderItemDao {

    void addOrderItem(TradeOrderItemPo TradeOrderItem);

}
