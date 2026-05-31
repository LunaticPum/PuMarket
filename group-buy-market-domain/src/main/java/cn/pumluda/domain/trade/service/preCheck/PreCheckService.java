package cn.pumluda.domain.trade.service.preCheck;

import cn.pumluda.domain.trade.adapter.repository.ITradeRepository;
import cn.pumluda.domain.trade.model.aggregate.OrderAggregate;
import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.SkuEntity;
import cn.pumluda.domain.trade.service.preCheck.dto.PreCheckResult;
import cn.pumluda.types.common.RedisConstants;
import cn.pumluda.types.enums.ResponseEnum;
import cn.pumluda.types.utils.RedisIdempotencyChecker;
import cn.pumluda.types.utils.juc.CompletableFutureUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Project: group-buy-market-better <p>
 * File: CreateOrderService <p>
 * Created by: 16374 <p>
 * Date: 2026/5/27 <p>
 * Time: 16:29 <p>
 * Description: 创建订单服务实现类
 */
@Service
@Slf4j
public class PreCheckService implements IPreCheckService {

    @Resource
    private CompletableFutureUtils asyncUtil;
    @Resource
    private RedisIdempotencyChecker idempotencyChecker;
    @Resource
    private ITradeRepository repository;

    @Override
    public Object preCheck(Long userId, String orderNo, Long activityId, Long skuId, int quantity) {
        /* 1. 前置基础校验：可并行执行 */
        log.info(
                """
                [创建订单] ========== 前置校验开始 ==========
                [创建订单] userId={}, activityId={}, skuId={}
                [创建订单] ================================
                """, userId, activityId, skuId
        );

        String bizId = userId + ":" + activityId + ":" + skuId;

        PreCheckResult validatedResult;
        try {
            /* 先做幂等性校验 */
            boolean idempotencyCheckResult = idempotencyChecker.tryAcquire(RedisConstants.CREATE_ORDER, bizId, 3);

            /* 再做并发查询 */
            List<Object> resultList = asyncUtil.supplyParallelWithTimeout(
                    2, TimeUnit.SECONDS,
                    /* 重复订单查询 */
                    () -> finalDuplicatedOrder(orderNo),
                    /* 商品库存校验 */
                    () -> findValidSkuBySkuId(skuId),
                    /* 活动有效性校验 */
                    () -> findValidActivityByActivityId(activityId)
            );

            validatedResult = new PreCheckResult(
                    idempotencyCheckResult,
                    (OrderAggregate) resultList.get(0),
                    (SkuEntity) resultList.get(1),
                    (ActivityConfigEntity) resultList.get(2),
                    bizId
            );
        } catch (TimeoutException e) {
            /* 响应异常：前置校验处理超时 */
            log.error(
                    "[创建订单] 前置校验超时 userId={}, skuId={}, activityId={}",
                    userId,
                    skuId,
                    activityId,
                    e
            );
            return ResponseEnum.TIME_OUT;
        } catch (Exception e) {
            /* 响应异常：前置校验处理出现未知错误 */
            log.error(
                    "[创建订单] 前置校验异常 userId={}, skuId={}, activityId={}",
                    userId,
                    skuId,
                    activityId,
                    e
            );
            return ResponseEnum.UN_ERROR;
        }

        Date curTime = new Date();
        final boolean firstCreate = validatedResult.isFirstCreate();
        final OrderAggregate order = validatedResult.getOrder();
        final SkuEntity sku = validatedResult.getSku();
        final ActivityConfigEntity activityConfig = validatedResult.getActivityConfig();

        /* 响应异常：1. 重复提交创建订单请求 */
        if (!firstCreate) {
            log.error(
                    "[创建订单] 重复请求幂等锁触发 userId={}, orderNo={}, activityId={}, skuId={}, bizId={}",
                    userId, orderNo, activityId, skuId, bizId
            );
            return ResponseEnum.DUPLICATE_CREATE_ORDER_REQUEST;
        }
        /* 响应异常：2. 交易订单已存在 */
        if (order != null) {
            log.error(
                    "[创建订单] 已存在交易订单 userId={}, orderNo={}, activityId={}, skuId={}",
                    userId, orderNo, activityId, skuId
            );
            return ResponseEnum.DUPLICATE_TRADE_ORDER;
        }
        /* 响应异常：3. 没找到对应商品 */
        if (sku == null) {
            log.error("[创建订单] 商品不存在 userId={}, orderNo={}, skuId={}", userId, orderNo, skuId);
            return ResponseEnum.SKU_NULL;
        }
        /* 响应异常：4. 没找到对应活动 */
        if (activityConfig == null) {
            log.error("[创建订单] 活动不存在 userId={}, orderNo={}, activityId={}", userId, orderNo, activityId);
            return ResponseEnum.ACTIVITY_NULL;
        }
        /* 响应异常：5. 商品库存不足 */
        // 商品卖完了
        if (sku.getStock() <= 0) {
            log.error(
                    "[创建订单] 商品已售罄 userId={}, orderNo={}, skuId={}, stock={}",
                    userId,
                    orderNo,
                    skuId,
                    sku.getStock()
            );
            return ResponseEnum.SKU_OUT_OF_STOCK;
        }
        // 商品不够卖
        if (sku.getStock() <= quantity) {
            log.error(
                    "[创建订单] 商品库存不足 userId={}, orderNo={}, skuId={}, requestedQuantity={}, stock={}",
                    userId, orderNo, skuId, quantity, sku.getStock()
            );
            return ResponseEnum.SKU_OUT_OF_STOCK;
        }
        /* 响应异常：6. 商品已下架 */
        if (sku.getStatus() == 0) {
            log.error("[创建订单] 商品已下架 userId={}, orderNo={}, skuId={}", userId, orderNo, skuId);
            return ResponseEnum.SKU_OFFLINE;
        }
        /* 响应异常：7. 活动未开启 */
        if (activityConfig.getStatus() == 0 || activityConfig.getStartTime().after(curTime)) {
            log.error(
                    "[创建订单] 活动未开始 userId={}, orderNo={}, activityId={}, activityStartTime={}",
                    userId, orderNo, activityId, activityConfig.getStartTime()
            );
            return ResponseEnum.ACTIVITY_NOT_STARTED;
        }
        /* 响应异常：8. 活动已过期 */
        if (activityConfig.getEndTime().before(curTime)) {
            log.error(
                    "[创建订单] 活动已结束 userId={}, orderNo={}, activityId={}, activityEndTime={}",
                    userId, orderNo, activityId, activityConfig.getEndTime()
            );
            return ResponseEnum.ACTIVITY_EXPIRED;
        }

        /* 前置校验通过则返回查询结果，以供后续链路使用 */
        log.info(
                """
                [创建订单] ========== 前置校验通过 ==========
                [创建订单] userId={},
                [创建订单] activityConfig={},
                [创建订单] sku={}
                [创建订单] ================================
                """, userId, JSON.toJSONString(activityConfig), JSON.toJSONString(sku)
        );
        return validatedResult;
    }

    @Override
    public SkuEntity findValidSkuBySkuId(Long skuId) {
        return repository.getSkuById(skuId);
    }

    @Override
    public OrderAggregate finalDuplicatedOrder(String orderNo) {
        return repository.getOrderByOrderNo(orderNo);
    }

    @Override
    public ActivityConfigEntity findValidActivityByActivityId(Long activityId) {
        return repository.getActivityByActivityId(activityId);
    }
}
