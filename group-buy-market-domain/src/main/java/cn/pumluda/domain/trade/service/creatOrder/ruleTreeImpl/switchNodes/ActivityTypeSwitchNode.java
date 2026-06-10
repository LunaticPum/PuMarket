package cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.switchNodes;

import cn.pumluda.domain.trade.model.aggregate.BusinessAggregate;
import cn.pumluda.domain.trade.model.entity.OrderItemEntity;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.core.context.DynamicContext;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.serviceNodes.defaultActivity.DefaultActivityNode;
import cn.pumluda.types.designs.ruleTree.AbstractStrategyRouter;
import cn.pumluda.types.designs.ruleTree.StrategyHandler;
import cn.pumluda.types.enums.ResponseEnum;
import cn.pumluda.types.exception.AppException;
import cn.pumluda.types.utils.UserTagUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Project: group-buy-market-better <p>
 * File: SwitchNode <p>
 * Created by: 16374 <p>
 * Date: 2026/5/28 <p>
 * Time: 17:13 <p>
 * Description: 活动分支节点 —— 根据活动类型分支
 */
@Slf4j
@Service
public class ActivityTypeSwitchNode extends AbstractStrategyRouter<BusinessAggregate, DynamicContext, OrderItemEntity> {

    @Resource
    private GroupTypeSwitchNode groupTypeSwitchNode;
    @Resource
    private DefaultActivityNode defaultActivityNode;

    @Override
    public OrderItemEntity apply(BusinessAggregate requestParam, DynamicContext dynamicContext) throws Exception {
        // 不做任何处理，直接进行分支路由
        return router(requestParam, dynamicContext);
    }

    @Override
    public StrategyHandler<BusinessAggregate, DynamicContext, OrderItemEntity> get(BusinessAggregate requestParam, DynamicContext dynamicContext) {
        Integer activityType = requestParam.getActivityConfig().getActivityType();

        /* 营销降级策略：目标人群限流 */
        Integer limitTag = requestParam.getActivityConfig().getLimitTag();
        if (limitTag != 0 && null != dynamicContext.getUserTagRecord()) {
            Integer userTag = dynamicContext.getUserTagRecord().getUserTag();
            // 利用二进制位图运算，高效识别指定标签人群
            if (UserTagUtil.hitAnyTag(userTag, limitTag)) {
                log.info(
                        "[创建订单] 命中活动限制标签，降级为普通订单 userId={} userTag={} limitTag={}",
                        requestParam.getUserId(),
                        userTag,
                        limitTag
                );

                // todo 先暂时设置为：只要是限流目标人群都走默认活动订单处理链路
                return defaultActivityNode;
            }
        }

        // todo 要添加其他活动处理链路，先走这里
        return switch (activityType) {
            case 0 -> defaultActivityNode;  // 默认活动订单处理链路
            case 1 -> groupTypeSwitchNode;  // 拼团活动订单处理链路
            default -> {
                log.error("[创建订单] 未识别到该活动类型 activityType={}", activityType);
                throw new AppException(
                        ResponseEnum.ILLEGAL_PARAMETER.getCode(),
                        ResponseEnum.ILLEGAL_PARAMETER.getInfo()
                );
            }
        };
    }
}
