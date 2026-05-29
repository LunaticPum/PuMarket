package cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.core;

import cn.pumluda.domain.trade.model.aggregate.BusinessAggregate;
import cn.pumluda.domain.trade.model.aggregate.OrderAggregate;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.RootNode;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.core.context.DynamicContext;
import cn.pumluda.types.designs.ruleTree.StrategyHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Project: group-buy-market-better <p>
 * File: RuleTreeFactory <p>
 * Created by: 16374 <p>
 * Date: 2026/5/28 <p>
 * Time: 17:11 <p>
 * Description: 规则树工厂类 —— 不同活动类型订单分支处理
 */
@Service
@RequiredArgsConstructor
public class RuleTreeFactory {

    private final RootNode rootNode;

    public StrategyHandler<BusinessAggregate, DynamicContext, OrderAggregate> getTreeRoot() {
        return rootNode;
    }

}
