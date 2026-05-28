package cn.pumluda.domain.trade.service.creatOrder;

import cn.pumluda.domain.trade.model.aggregate.BusinessAggregate;
import cn.pumluda.domain.trade.model.aggregate.OrderAggregate;
import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.SkuEntity;
import cn.pumluda.types.enums.ResponseEnum;


/**
 * Project: group-buy-market-better <p>
 * File: ICreateOrderService <p>
 * Created by: 16374 <p>
 * Date: 2026/5/27 <p>
 * Time: 09:39 <p>
 * Description: 创建订单服务抽象
 */
public interface ICreateOrderService {

    /**
     * 创建订单，根据订单参与的活动类型执行不同的订单处理逻辑
     * @param businessAggregate 执行链路所需的基本信息
     * @return 要新增的订单主表记录（包括订单明细表记录）
     */
    OrderAggregate createOrder(BusinessAggregate businessAggregate) throws Exception;

}
