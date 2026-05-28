package cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl;

import cn.pumluda.domain.trade.model.aggregate.BusinessAggregate;
import cn.pumluda.domain.trade.model.aggregate.OrderAggregate;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.core.context.DynamicContext;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.switchNodes.ActivityTypeSwitchNode;
import cn.pumluda.types.designs.ruleTree.AbstractStrategyRouter;
import cn.pumluda.types.designs.ruleTree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Project: group-buy-market-better <p>
 * File: RootNode <p>
 * Created by: 16374 <p>
 * Date: 2026/5/28 <p>
 * Time: 17:12 <p>
 * Description: 根节点 —— 规则树入口节点
 */
@Slf4j
@Service
public class RootNode extends AbstractStrategyRouter<BusinessAggregate, DynamicContext, OrderAggregate> {

    @Resource
    private ActivityTypeSwitchNode activityTypeSwitchNode;

    @Override
    public OrderAggregate apply(BusinessAggregate requestParameter, DynamicContext dynamicContext) throws Exception {
        return null;
    }

    @Override
    public StrategyHandler<BusinessAggregate, DynamicContext, OrderAggregate> get(BusinessAggregate requestParameter, DynamicContext dynamicContext) {
        return null;
    }
}
