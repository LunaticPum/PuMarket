package cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.serviceNodes.groupBuyActivity;

import cn.hutool.core.date.DateUtil;
import cn.pumluda.domain.trade.adapter.repository.ITradeRepository;
import cn.pumluda.domain.trade.model.aggregate.BusinessAggregate;
import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.ActivityOrderRecordEntity;
import cn.pumluda.domain.trade.model.entity.GroupTeamEntity;
import cn.pumluda.domain.trade.model.entity.OrderItemEntity;
import cn.pumluda.domain.trade.model.valobj.GroupTeamStatusEnumVo;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.core.context.DynamicContext;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.serviceNodes.OrderDiscountCalcNode;
import cn.pumluda.types.designs.ruleTree.AbstractStrategyRouter;
import cn.pumluda.types.designs.ruleTree.StrategyHandler;
import cn.pumluda.types.enums.ActivityParticipationTypeEnum;
import cn.pumluda.types.enums.ResponseEnum;
import cn.pumluda.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

/**
 * Project: group-buy-market-better <p>
 * File: NormalOrderNode <p>
 * Created by: 16374 <p>
 * Date: 2026/5/29 <p>
 * Time: 10:25 <p>
 * Description: 拼团活动订单处理节点 —— 拼团方式为参团
 */
@Slf4j
@Service
public class JoinGroupNode extends AbstractStrategyRouter<BusinessAggregate, DynamicContext, OrderItemEntity> {

    @Resource
    private ITradeRepository repository;
    @Resource
    private OrderDiscountCalcNode orderDiscountCalcNode;

    @Override
    public OrderItemEntity apply(BusinessAggregate requestParam, DynamicContext dynamicContext) throws Exception {

        Long groupTeamId = requestParam.getGroupTeamId();
        ActivityConfigEntity activityConfig = requestParam.getActivityConfig();
        GroupTeamEntity groupTeam = repository.getGroupTeam(activityConfig.getActivityId(), groupTeamId);

        if (null == groupTeam) {
            log.error("[创建订单-拼团活动订单] 该拼团队伍不存在 groupTeamId={}", groupTeamId);
            throw new AppException(ResponseEnum.GROUP_TEAM_NULL.getCode(), ResponseEnum.GROUP_TEAM_NULL.getInfo());
        } else if (!groupTeam.getTeamStatus().equals(GroupTeamStatusEnumVo.PROGRESS)) {
            log.error("[创建订单-拼团活动订单] 该拼团队伍已不可参与 groupTeamId={}", groupTeamId);
            throw new AppException(ResponseEnum.GROUP_TEAM_FAIL.getCode(), ResponseEnum.GROUP_TEAM_FAIL.getInfo());
        } else if (Objects.equals(groupTeam.getCurrentNum(), groupTeam.getRequiredNum())) {
            log.error("[创建订单-拼团活动订单] 该拼团队伍已满 groupTeamId={}", groupTeamId);
            throw new AppException(ResponseEnum.GROUP_TEAM_FULL.getCode(), ResponseEnum.GROUP_TEAM_FULL.getInfo());
        } else if (!Objects.equals(groupTeam.getSkuId(), requestParam.getSku().getSkuId())) {
            log.error("[创建订单-拼团活动订单] 用户拼团购买的商品和拼团队伍绑定的商品不一致 groupTeamId={}", groupTeamId);
            throw new AppException(ResponseEnum.ILLEGAL_PARAMETER.getCode(), "购买与拼团商品不一致");
        }

        Date now = new Date();
        Date expireTime = DateUtil.offsetDay(now, 1);   // 拼团队伍有效期限为 1 天

        ActivityOrderRecordEntity activityOrderRecord = ActivityOrderRecordEntity.builder()
                                                                                 .orderNo(requestParam.getOrderNo())
                                                                                 .userId(requestParam.getUserId())
                                                                                 .activityId(activityConfig.getActivityId())
                                                                                 .activityName(activityConfig.getActivityName())
                                                                                 .activityBusinessId(groupTeamId)
                                                                                 .participationType(
                                                                                         ActivityParticipationTypeEnum.JOIN_GROUP)
                                                                                 .quotaOccupied(1)
                                                                                 .recordStatus(0)
                                                                                 .joinTime(now)
                                                                                 .expireTime(expireTime)
                                                                                 .build();

        dynamicContext.setActivityBusinessId(groupTeamId);
        repository.addActivityOrderRecord(activityOrderRecord);
        repository.joinGroupTeam(activityConfig.getActivityId(), groupTeamId);
        repository.reserveSkuStock(requestParam.getSku().getSkuId(), requestParam.getQuantity());

        log.info(
                """
                [创建订单-拼团活动订单] ========== 已新增参团记录与活动参与记录，商品库存已预锁 ==========
                [创建订单-拼团活动订单] userId={} orderNo={} activityId={} groupTeamId={}
                [创建订单-拼团活动订单] skuId={} productName={} quantity={}
                [创建订单-拼团活动订单] ===========================================================
                """,
                requestParam.getUserId(),
                requestParam.getOrderNo(),
                activityConfig.getActivityId(),
                groupTeamId,
                requestParam.getSku().getSkuId(),
                requestParam.getSku().getProductName(),
                requestParam.getQuantity()
        );


        return router(requestParam, dynamicContext);
    }

    @Override
    public StrategyHandler<BusinessAggregate, DynamicContext, OrderItemEntity> get(BusinessAggregate requestParam, DynamicContext dynamicContext) {
        return orderDiscountCalcNode;
    }
}
