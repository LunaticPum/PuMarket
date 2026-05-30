package cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl;

import cn.pumluda.domain.trade.adapter.repository.ITradeRepository;
import cn.pumluda.domain.trade.model.aggregate.BusinessAggregate;
import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.OrderItemEntity;
import cn.pumluda.domain.trade.model.entity.SkuEntity;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.core.context.DynamicContext;
import cn.pumluda.types.common.RedisConstants;
import cn.pumluda.types.designs.ruleTree.AbstractStrategyRouter;
import cn.pumluda.types.designs.ruleTree.StrategyHandler;
import cn.pumluda.types.utils.RedisKeyBuilder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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

    @Resource
    private ITradeRepository repository;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public OrderItemEntity apply(BusinessAggregate requestParam, DynamicContext dynamicContext) throws Exception {

        log.info("[创建订单-数据落库] 开始数据持久化");

        SkuEntity sku = requestParam.getSku();
        ActivityConfigEntity activityConfig = requestParam.getActivityConfig();
        Long activityBusinessId = dynamicContext.getActivityBusinessId();

        OrderItemEntity orderItem = OrderItemEntity.builder()
                                                   .orderNo(requestParam.getOrderNo())
                                                   .skuId(sku.getSkuId())
                                                   .productName(sku.getProductName())
                                                   .skuPrice(sku.getPrice())
                                                   .quantity(requestParam.getQuantity())
                                                   .actualPrice(dynamicContext.getActualPrice())
                                                   .activityId(activityConfig.getActivityId())
                                                   .activityBusinessId(activityBusinessId)
                                                   .build();

        repository.addOrderItem(orderItem);
        repository.updateTradeOrder(orderItem);

        /* 拼团锁释放 */
        String lockKey = RedisKeyBuilder.buildKey(
                RedisConstants.LOCK_GROUP_ORDER,
                activityBusinessId
        );
        RLock lock = redissonClient.getLock(lockKey);

        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }

        log.info("[创建订单-数据落库] 完成");

        log.info(
                """
                [创建订单] ========== 订单创建业务完成 ==========
                [创建订单] userId={}, orderNo={}
                [创建订单] ===================================
                """, requestParam.getUserId(), requestParam.getOrderNo()
        );

        return orderItem;
    }

    @Override
    public StrategyHandler<BusinessAggregate, DynamicContext, OrderItemEntity> get(BusinessAggregate requestParam, DynamicContext dynamicContext) {
        return null;
    }
}
