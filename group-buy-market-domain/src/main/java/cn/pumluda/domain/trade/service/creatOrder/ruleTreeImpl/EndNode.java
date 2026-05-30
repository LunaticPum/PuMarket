package cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl;

import cn.pumluda.domain.trade.model.aggregate.BusinessAggregate;
import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.GroupTeamEntity;
import cn.pumluda.domain.trade.model.entity.OrderItemEntity;
import cn.pumluda.domain.trade.model.entity.SkuEntity;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.core.context.DynamicContext;
import cn.pumluda.types.designs.ruleTree.AbstractStrategyRouter;
import cn.pumluda.types.designs.ruleTree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Project: group-buy-market-better <p>
 * File: EndNode <p>
 * Created by: 16374 <p>
 * Date: 2026/5/30 <p>
 * Time: 10:18 <p>
 * Description: 链路终点 —— 记录数据落库
 */
@Slf4j
@Service
public class EndNode extends AbstractStrategyRouter<BusinessAggregate, DynamicContext, OrderItemEntity> {

    @Override
    public OrderItemEntity apply(BusinessAggregate requestParam, DynamicContext dynamicContext) throws Exception {

        log.info("[创建订单-数据落库] 开始数据持久化");

        SkuEntity sku = requestParam.getSku();
        ActivityConfigEntity activityConfig = requestParam.getActivityConfig();
        GroupTeamEntity groupTeam = dynamicContext.getGroupTeam();

        OrderItemEntity orderItem = OrderItemEntity.builder()
                                                   .orderNo(requestParam.getOrderNo())
                                                   .skuId(sku.getSkuId())
                                                   .productName(sku.getProductName())
                                                   .price(sku.getPrice())
                                                   .quantity(requestParam.getQuantity())
                                                   .actualPrice(dynamicContext.getActualPrice())
                                                   .activityId(activityConfig.getActivityId())
                                                   .groupTeamId(groupTeam.getGroupTeamId())
                                                   .build();

        // todo 落库 + 消息通知。。

        log.info("[创建订单-数据落库] 完成");

        return orderItem;
    }

    @Override
    public StrategyHandler<BusinessAggregate, DynamicContext, OrderItemEntity> get(BusinessAggregate requestParam, DynamicContext dynamicContext) {
        return null;
    }
}
