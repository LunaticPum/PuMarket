package cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl;

import cn.pumluda.domain.trade.adapter.repository.ITradeRepository;
import cn.pumluda.domain.trade.model.aggregate.BusinessAggregate;
import cn.pumluda.domain.trade.model.aggregate.OrderAggregate;
import cn.pumluda.domain.trade.model.entity.UserTagRecordEntity;
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
    @Resource
    private ITradeRepository repository;

    @Override
    public OrderAggregate apply(BusinessAggregate requestParam, DynamicContext dynamicContext) throws Exception {
        /* 识别用户标签记录，若有记录则存到上下文，供链路的后续服务实现使用 */
        Long userId = requestParam.getUserId();
        String orderNo = requestParam.getOrderNo();

        log.info(
                """
                [创建订单] ========== 订单创建业务开始 ==========
                [创建订单] userId={}, orderNo={}
                [创建订单] ===================================
                """, userId, orderNo
        );

        UserTagRecordEntity userTagRecord = repository.getUserTagRecordById(userId);
        if (userTagRecord != null) {
            log.info(
                    "[创建订单] 命中用户标签记录 用户 ID：{} 用户标签：{}",
                    userTagRecord.getUserId(),
                    userTagRecord.getUserTag()
            );

            dynamicContext.setUserTagRecord(userTagRecord);
        }

        return router(requestParam, dynamicContext);
    }

    @Override
    public StrategyHandler<BusinessAggregate, DynamicContext, OrderAggregate> get(BusinessAggregate requestParam, DynamicContext dynamicContext) {
        return activityTypeSwitchNode;
    }
}
