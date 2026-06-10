package cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.serviceNodes.defaultActivity;

import cn.pumluda.domain.trade.adapter.repository.ITradeRepository;
import cn.pumluda.domain.trade.model.aggregate.BusinessAggregate;
import cn.pumluda.domain.trade.model.entity.OrderItemEntity;
import cn.pumluda.domain.trade.model.entity.SkuEntity;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.EndNode;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.core.context.DynamicContext;
import cn.pumluda.types.designs.ruleTree.AbstractStrategyRouter;
import cn.pumluda.types.designs.ruleTree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * 默认活动（无优惠）订单处理 —— 单独购买走这条分支
 */
@Slf4j
@Service
public class DefaultActivityNode extends AbstractStrategyRouter<BusinessAggregate, DynamicContext, OrderItemEntity> {

    @Resource
    private ITradeRepository repository;
    @Resource
    private EndNode endNode;

    @Override
    public OrderItemEntity apply(BusinessAggregate requestParam, DynamicContext dynamicContext) throws Exception {
        SkuEntity sku = requestParam.getSku();
        int quantity = requestParam.getQuantity();

        log.info("[创建订单-默认活动] 无优惠订单 skuId={} quantity={} price={}", sku.getSkuId(), quantity, sku.getPrice());

        // 预占库存
        repository.reserveSkuStock(sku.getSkuId(), quantity);

        // 无优惠：原价 = 实付
        BigDecimal originPrice = sku.getPrice();
        BigDecimal totalAmount = originPrice.multiply(BigDecimal.valueOf(quantity));
        dynamicContext.setActualPrice(totalAmount);
        dynamicContext.setDiscountPrice(BigDecimal.ZERO);
        dynamicContext.setTotalAmount(totalAmount);

        return router(requestParam, dynamicContext);
    }

    @Override
    public StrategyHandler<BusinessAggregate, DynamicContext, OrderItemEntity> get(BusinessAggregate requestParam, DynamicContext dynamicContext) {
        return endNode;
    }
}
