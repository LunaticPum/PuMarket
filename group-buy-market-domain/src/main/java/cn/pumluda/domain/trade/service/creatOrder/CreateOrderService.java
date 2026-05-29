package cn.pumluda.domain.trade.service.creatOrder;

import cn.pumluda.domain.trade.model.aggregate.BusinessAggregate;
import cn.pumluda.domain.trade.model.aggregate.OrderAggregate;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.core.RuleTreeFactory;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.core.context.DynamicContext;
import cn.pumluda.types.designs.ruleTree.StrategyHandler;
import cn.pumluda.types.enums.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

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

    @Resource
    private RuleTreeFactory factory;

    /**
     * 先写 DB 再异步更新 Redis Cache，只保证最终一致性
     * @param businessAggregate 执行链路所需的基本信息
     * @return 要落库的订单主表和明细表信息
     * @throws Exception 规则树中任意节点出现的异常
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public OrderAggregate createOrder(BusinessAggregate businessAggregate) throws Exception {

        StrategyHandler<BusinessAggregate, DynamicContext, OrderAggregate> root = factory.getTreeRoot();

        // todo 规则树编排实现
        return root.apply(businessAggregate, new DynamicContext());
    }
}
