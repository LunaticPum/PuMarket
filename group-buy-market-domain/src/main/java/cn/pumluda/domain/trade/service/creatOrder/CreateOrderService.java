package cn.pumluda.domain.trade.service.creatOrder;

import cn.pumluda.domain.trade.model.aggregate.BusinessAggregate;
import cn.pumluda.domain.trade.model.aggregate.OrderAggregate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Project: group-buy-market-better <p>
 * File: CreateOrderService <p>
 * Created by: 16374 <p>
 * Date: 2026/5/28 <p>
 * Time: 18:04 <p>
 * Description: 创建订单服务实现
 */
@Slf4j
@Service
public class CreateOrderService implements ICreateOrderService {
    @Override
    public OrderAggregate createOrder(BusinessAggregate businessAggregate) {

        // todo 规则树编排实现


        return null;
    }
}
