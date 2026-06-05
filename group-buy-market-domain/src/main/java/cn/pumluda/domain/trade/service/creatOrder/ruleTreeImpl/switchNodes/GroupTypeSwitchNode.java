package cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.switchNodes;

import cn.pumluda.domain.trade.adapter.repository.ITradeRepository;
import cn.pumluda.domain.trade.model.aggregate.BusinessAggregate;
import cn.pumluda.domain.trade.model.entity.ActivityOrderRecordEntity;
import cn.pumluda.domain.trade.model.entity.OrderItemEntity;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.core.context.DynamicContext;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.serviceNodes.groupBuyActivity.CreateGroupNode;
import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.serviceNodes.groupBuyActivity.JoinGroupNode;
import cn.pumluda.types.common.RedisConstants;
import cn.pumluda.types.designs.ruleTree.AbstractStrategyRouter;
import cn.pumluda.types.designs.ruleTree.StrategyHandler;
import cn.pumluda.types.enums.ResponseEnum;
import cn.pumluda.types.exception.AppException;
import cn.pumluda.types.utils.RedisKeyBuilder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Project: group-buy-market-better <p>
 * File: GroupTypeSwitchNode <p>
 * Created by: 16374 <p>
 * Date: 2026/5/29 <p>
 * Time: 10:06 <p>
 * Description: 拼团分支节点 —— 根据参团方式分支
 */

@Slf4j
@Service
public class GroupTypeSwitchNode extends AbstractStrategyRouter<BusinessAggregate, DynamicContext, OrderItemEntity> {

    @Resource
    private ITradeRepository repository;

    @Resource
    private CreateGroupNode createGroupNode;

    @Resource
    private JoinGroupNode joinGroupNode;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public OrderItemEntity apply(BusinessAggregate requestParam, DynamicContext dynamicContext) throws Exception {

        String orderNo = requestParam.getOrderNo();
        Long activityId = requestParam.getActivityConfig().getActivityId();
        Long groupTeamId = requestParam.getGroupTeamId();

        log.info("[创建订单] 活动类型识别成功，识别为拼团活动订单 orderNo={}", orderNo);

        /* 锁粒度：拼团队伍 ID，确保入团人数和优惠名额占用状态不会并发冲突 */
        String lockKey = RedisKeyBuilder.buildKey(RedisConstants.LOCK_GROUP_ORDER, groupTeamId);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            /* 拼团活动业务起点：同一个团内的所有订单创建请求进来一律先加锁 */
            boolean locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                log.warn(
                        "[创建订单-拼团活动订单] 订单 {} 阻塞中，请等待团 {} 内的其他订单处理完成",
                        orderNo,
                        groupTeamId
                );
            } else {
                log.info(
                        "[创建订单-拼团活动订单] 持锁成功，开始执行订单 {} 的拼团活动处理链路",
                        orderNo
                );
            }

            /* 1. 查询用户在当前活动中是否已有拼团活动参与记录：一个活动中每个用户只能同时有一个活动参与且处理中记录， */
            ActivityOrderRecordEntity activityOrderRecord = repository.getActivityOrderRecord(
                    requestParam.getUserId(),
                    activityId
            );

            /* 2. 校验拼团记录是否仍处于处理中状态 */
            if (activityOrderRecord != null) {
                Date expireTime = activityOrderRecord.getExpireTime();
                boolean isNotExpired = expireTime == null || expireTime.after(new Date());
                boolean isProcessing = activityOrderRecord.getRecordStatus() == 0;
                log.info("RecordStatus = {}, isProcessing = {}", activityOrderRecord.getRecordStatus(), isProcessing);
                /* 已存在正在处理中的拼团记录，当前订单不可重复参与拼团。 */
                if (isNotExpired && isProcessing) {
                    log.error(
                            "[创建订单-拼团活动订单] 已存在正在处理中的拼团记录，不可重复参与拼团 userId={} activityId={} orderNo={}",
                            requestParam.getUserId(),
                            activityId,
                            orderNo
                    );
                    throw new AppException(
                            ResponseEnum.ORDER_HAS_GROUP.getCode(),
                            ResponseEnum.ORDER_HAS_GROUP.getInfo()
                    );
                }
            }

            return router(requestParam, dynamicContext);
        } finally {
            // 确保锁释放，同时避免误释放其他线程的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }

    @Override
    public StrategyHandler<BusinessAggregate, DynamicContext, OrderItemEntity> get(BusinessAggregate requestParam, DynamicContext dynamicContext) {
        /* 查询用户参团方式 */
        if (null == requestParam.getGroupTeamId()) {
            return createGroupNode;
        } else {
            return joinGroupNode;
        }
    }
}
