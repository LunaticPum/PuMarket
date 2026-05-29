package cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.serviceNodes.groupBuyActivity;

import cn.pumluda.domain.trade.adapter.repository.ITradeRepository;
import cn.pumluda.domain.trade.model.aggregate.BusinessAggregate;
import cn.pumluda.domain.trade.model.aggregate.OrderAggregate;
import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.SkuEntity;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.core.context.DynamicContext;
import cn.pumluda.types.designs.ruleTree.AbstractStrategyRouter;
import cn.pumluda.types.designs.ruleTree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Project: group-buy-market-better <p>
 * File: OrderDiscountCalcNode <p>
 * Created by: 16374 <p>
 * Date: 2026/5/29 <p>
 * Time: 18:42 <p>
 * Description: 优惠价格试算
 */
@Slf4j
@Service
public class OrderDiscountCalcNode extends AbstractStrategyRouter<BusinessAggregate, DynamicContext, OrderAggregate> {

    @Resource
    private ITradeRepository repository;

    @Override
    public OrderAggregate apply(BusinessAggregate requestParam, DynamicContext dynamicContext) throws Exception {
        SkuEntity sku = requestParam.getSku();
        int quantity = requestParam.getQuantity();
        String discountExpr = requestParam.getActivityConfig().getDiscountExpr();

        // todo 优惠价格试算逻辑，根据不同优惠类型处理不同优惠表达式

        return null;
    }

    @Override
    public StrategyHandler<BusinessAggregate, DynamicContext, OrderAggregate> get(BusinessAggregate requestParam, DynamicContext dynamicContext) {
        return null;
    }

}

