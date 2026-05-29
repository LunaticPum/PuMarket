package cn.pumluda.domain.trade.service.preCheck;

import cn.pumluda.domain.trade.adapter.repository.ITradeRepository;
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
    public Object preCheck(Long userId, Long activityId, Long skuId, int quantity) {
        /* 1. 前置基础校验：可并行执行 */
        log.info(
                """
                [创建订单] ========== 前置校验开始 ==========
                [创建订单] userId={}, skuId={}, activityId={}
                [创建订单] ================================
                """, userId, skuId, activityId
        );

        PreCheckResult validatedResult;
        try {
            List<Object> resultList = asyncUtil.supplyParallelWithTimeout(
                    2, TimeUnit.SECONDS,
                    /* 幂等性校验：每个用户在每 5 秒内只能发起一次创建订单请求 */
                    () -> idempotencyChecker.tryAcquire(RedisConstants.CREATE_ORDER, userId, 5),
                    /* 商品库存校验 */
                    () -> findValidSkuBySkuId(skuId),
                    /* 活动有效性校验 */
                    () -> findValidActivityByActivityId(activityId)
            );

            validatedResult = new PreCheckResult(
                    Boolean.TRUE.equals(resultList.get(0)),
                    (SkuEntity) resultList.get(1),
                    (ActivityConfigEntity) resultList.get(2)
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
        final SkuEntity sku = validatedResult.getSku();
        final ActivityConfigEntity activityConfig = validatedResult.getActivityConfig();

        /* 响应异常：1. 重复提交创建订单请求 */
        if (!firstCreate) return ResponseEnum.DUPLICATE_ORDER_REQUEST;
        /* 响应异常：2. 没找到对应商品 */
        if (sku == null) return ResponseEnum.SKU_NULL;
        /* 响应异常：3. 没找到对应活动 */
        if (activityConfig == null) return ResponseEnum.ACTIVITY_NULL;
        /* 响应异常：4. 商品库存不足 */
        // 商品卖完了
        if (sku.getStock() <= 0) return ResponseEnum.SKU_OUT_OF_STOCK;
        // 商品不够卖
        if (sku.getStock() <= quantity) return ResponseEnum.SKU_OUT_OF_STOCK;
        /* 响应异常：5. 商品已下架 */
        if (sku.getStatus() == 0) return ResponseEnum.SKU_OFFLINE;
        /* 响应异常：6. 活动未开启 */
        if (activityConfig.getStatus() == 0 || activityConfig.getStartTime().after(curTime))
            return ResponseEnum.ACTIVITY_NOT_STARTED;
        /* 响应异常：7. 活动已过期 */
        if (activityConfig.getEndTime().before(curTime)) return ResponseEnum.ACTIVITY_EXPIRED;

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
    public ActivityConfigEntity findValidActivityByActivityId(Long activityId) {
        return repository.getActivityByActivityId(activityId);
    }
}
