package cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.serviceNodes.groupBuyActivity;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.pumluda.domain.trade.adapter.repository.ITradeRepository;
import cn.pumluda.domain.trade.model.aggregate.BusinessAggregate;
import cn.pumluda.domain.trade.model.aggregate.OrderAggregate;
import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.ActivityOrderRecordEntity;
import cn.pumluda.domain.trade.model.entity.GroupTeamEntity;
import cn.pumluda.domain.trade.model.valobj.GroupTeamStatusEnumVo;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.core.context.DynamicContext;
import cn.pumluda.types.designs.ruleTree.AbstractStrategyRouter;
import cn.pumluda.types.designs.ruleTree.StrategyHandler;
import cn.pumluda.types.enums.ActivityParticipationTypeEnum;
import cn.pumluda.types.enums.ResponseEnum;
import cn.pumluda.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Project: group-buy-market-better <p>
 * File: CreateGroupNode <p>
 * Created by: 16374 <p>
 * Date: 2026/5/29 <p>
 * Time: 11:03 <p>
 * Description: 拼团活动订单处理节点 —— 拼团方式为开团
 */

@Slf4j
@Service
public class CreateGroupNode extends AbstractStrategyRouter<BusinessAggregate, DynamicContext, OrderAggregate> {

    // todo 首次使用 HuTool，利用雪花算法生成全局唯一 ID
    private final Snowflake snowflake = IdUtil.getSnowflake(1, 1);
    @Resource
    private ITradeRepository repository;
    @Resource
    private OrderDiscountCalcNode orderDiscountCalcNode;

    @Override
    public OrderAggregate apply(BusinessAggregate requestParam, DynamicContext dynamicContext) throws Exception {

        long groupTeamId = snowflake.nextId();
        log.info("[创建订单-拼团活动订单] 拼团方式：开团 已分配拼团队伍 ID：{}", groupTeamId);

        ActivityConfigEntity activityConfig = requestParam.getActivityConfig();
        Date now = new Date();
        Date expireTime = DateUtil.offsetDay(now, 1);   // 拼团队伍有效期限为 1 天

        GroupTeamEntity groupTeam = GroupTeamEntity.builder()
                                                   .activityId(activityConfig.getActivityId())
                                                   .groupTeamId(groupTeamId)
                                                   .leaderUserId(requestParam.getUserId())
                                                   .requiredNum(activityConfig.getTotalDiscountQuota())
                                                   .currentNum(1)
                                                   .settledTradeNum(0)
                                                   .teamStatus(GroupTeamStatusEnumVo.PROGRESS)
                                                   .teamCreateTime(now)
                                                   .teamExpireTime(expireTime)
                                                   .build();

        ActivityOrderRecordEntity activityOrderRecord = ActivityOrderRecordEntity.builder()
                                                                                 .orderNo(
                                                                                         requestParam.getOrderNo())
                                                                                 .userId(requestParam.getUserId())
                                                                                 .activityId(
                                                                                         activityConfig.getActivityId())
                                                                                 .activityName(
                                                                                         activityConfig.getActivityName())
                                                                                 .activityBusinessId(
                                                                                         groupTeamId)
                                                                                 .participationType(
                                                                                         ActivityParticipationTypeEnum.CREATE_GROUP)
                                                                                 .quotaOccupied(1)
                                                                                 .recordStatus(0)
                                                                                 .joinTime(now)
                                                                                 .expireTime(
                                                                                         expireTime)
                                                                                 .build();

        // 这里写失败了 jdbc 会自动抛异常触发事务回滚
        repository.addActivityOrderRecord(activityOrderRecord);
        repository.addGroupTeam(groupTeam);

        /* 为订单预占商品库存：MySQL InnoDB 会对商品 SKU 表中的匹配行加行锁，保证 UPDATE 原子操作 */
        int result = repository.reserveSkuStock(
                requestParam.getSku().getSkuId(),
                requestParam.getQuantity()
        );

        if (result == 0) {
            throw new AppException(
                    ResponseEnum.SKU_OUT_OF_STOCK.getCode(),
                    ResponseEnum.SKU_OUT_OF_STOCK.getInfo()
            );
        }

        log.info(
                """
                [创建订单-拼团活动订单] ========== 已新增拼团队伍与活动参与记录，商品库存已预锁 ==========
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
    public StrategyHandler<BusinessAggregate, DynamicContext, OrderAggregate> get(BusinessAggregate requestParam, DynamicContext dynamicContext) {
        return orderDiscountCalcNode;
    }
}
