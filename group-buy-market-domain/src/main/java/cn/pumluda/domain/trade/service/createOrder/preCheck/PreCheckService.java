package cn.pumluda.domain.trade.service.createOrder.preCheck;

import cn.pumluda.domain.trade.adapter.repository.ITradeRepository;
import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.SkuEntity;
import cn.pumluda.domain.trade.service.createOrder.ICreateOrderService;
import cn.pumluda.domain.trade.service.createOrder.IPreCheckService;
import cn.pumluda.domain.trade.service.createOrder.dto.CreateOrderPreCheckResult;
import cn.pumluda.types.common.RedisConstants;
import cn.pumluda.types.enums.ResponseEnum;
import cn.pumluda.types.utils.juc.CompletableFutureUtils;
import cn.pumluda.types.utils.RedisIdempotencyChecker;
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
    public ResponseEnum preCheck(String userId, Long activityId, Long skuId) {
        /* 1. 前置基础校验：可并行执行 */
        CreateOrderPreCheckResult validateResult;
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

            validateResult = new CreateOrderPreCheckResult(
                    Boolean.TRUE.equals(resultList.get(0)),
                    (SkuEntity) resultList.get(1),
                    (ActivityConfigEntity) resultList.get(2)
            );
        } catch (TimeoutException e) {
            log.error(
                    "[创建订单] 前置校验超时 userId={}, skuId={}, activityId={}",
                    userId,
                    skuId,
                    activityId,
                    e
            );
            return ResponseEnum.TIME_OUT;
        } catch (Exception e) {
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
        final boolean firstCreate = validateResult.isFirstCreate();
        final SkuEntity sku = validateResult.getSku();
        final ActivityConfigEntity activityConfig = validateResult.getActivityConfig();

        if (!firstCreate) return ResponseEnum.DUPLICATE_ORDER_REQUEST;

        if (sku == null) return ResponseEnum.SKU_NULL;

        if (activityConfig == null) return ResponseEnum.ACTIVITY_NULL;

        if (sku.getStock() <= 0) return ResponseEnum.SKU_OUT_OF_STOCK;

        if (sku.getStatus() == 0) return ResponseEnum.SKU_OFFLINE;

        if (activityConfig.getStatus() == 0 || activityConfig.getStartTime().after(curTime))
            return ResponseEnum.ACTIVITY_NOT_STARTED;

        if (activityConfig.getEndTime().before(curTime)) return ResponseEnum.ACTIVITY_EXPIRED;

        log.info(
                "[创建订单] 前置校验通过，活动：{} 商品：{}",
                JSON.toJSONString(activityConfig),
                JSON.toJSONString(sku)
        );

        // 前置校验通过则返回 null
        return null;
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
