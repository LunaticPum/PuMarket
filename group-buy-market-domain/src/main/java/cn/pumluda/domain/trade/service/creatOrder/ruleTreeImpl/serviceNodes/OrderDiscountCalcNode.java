package cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.serviceNodes;

import cn.pumluda.domain.trade.model.aggregate.BusinessAggregate;
import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.OrderItemEntity;
import cn.pumluda.domain.trade.model.entity.SkuEntity;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.EndNode;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.core.context.DynamicContext;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.discountCalcStrategy.DiscountCalculator;
import cn.pumluda.types.designs.ruleTree.AbstractStrategyRouter;
import cn.pumluda.types.designs.ruleTree.StrategyHandler;
import cn.pumluda.types.enums.DiscountTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;

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
public class OrderDiscountCalcNode extends AbstractStrategyRouter<BusinessAggregate, DynamicContext, OrderItemEntity> {

    @Resource
    private EndNode endNode;
    @Resource
    private Map<String, DiscountCalculator> discountCalculatorMap;

    @Override
    public OrderItemEntity apply(BusinessAggregate requestParam, DynamicContext dynamicContext) throws Exception {

        log.info("[创建订单] 优惠价格试算中 ...");

        SkuEntity sku = requestParam.getSku();
        int quantity = requestParam.getQuantity();
        ActivityConfigEntity activityConfig = requestParam.getActivityConfig();

        DiscountTypeEnum discountType = activityConfig.getDiscountType();
        Map<String, Object> discountConfig = activityConfig.getDiscountConfig();

        // 按照预定义的优惠配置枚举，获取对应的优惠计算器
        DiscountCalculator discountCalculator = discountCalculatorMap.get(discountType.getName());
        BigDecimal[] calculateResult = discountCalculator.calculate(
                sku.getPrice(),
                quantity,
                discountConfig
        );

        dynamicContext.setTotalAmount(sku.getPrice().multiply(BigDecimal.valueOf(quantity)));
        dynamicContext.setDiscountPrice(calculateResult[0]);
        dynamicContext.setActualPrice(calculateResult[1]);

        return router(requestParam, dynamicContext);
    }

    @Override
    public StrategyHandler<BusinessAggregate, DynamicContext, OrderItemEntity> get(BusinessAggregate requestParam, DynamicContext dynamicContext) {
        return endNode;
    }

}

