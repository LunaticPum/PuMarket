package cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.serviceNodes.defaultActivity;

import cn.pumluda.domain.trade.model.aggregate.BusinessAggregate;
import cn.pumluda.domain.trade.model.aggregate.OrderAggregate;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.core.context.DynamicContext;
import cn.pumluda.types.designs.ruleTree.AbstractStrategyRouter;
import cn.pumluda.types.designs.ruleTree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Project: group-buy-market-better <p>
 * File: NormalOrderNode <p>
 * Created by: 16374 <p>
 * Date: 2026/5/29 <p>
 * Time: 10:25 <p>
 * Description: 默认活动订单处理节点 —— 不参与活动的订单统一走这条分支
 */
@Slf4j
@Service
public class DefaultActivityNode extends AbstractStrategyRouter<BusinessAggregate, DynamicContext, OrderAggregate> {

    @Override
    public OrderAggregate apply(BusinessAggregate requestParam, DynamicContext dynamicContext) throws Exception {
        return null;
    }

    @Override
    public StrategyHandler<BusinessAggregate, DynamicContext, OrderAggregate> get(BusinessAggregate requestParam, DynamicContext dynamicContext) {
        return null;
    }
}
