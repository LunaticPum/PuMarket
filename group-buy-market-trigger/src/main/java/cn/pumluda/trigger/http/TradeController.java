package cn.pumluda.trigger.http;

import cn.pumluda.api.ITradeController;
import cn.pumluda.api.dto.CreateOrderReqDTO;
import cn.pumluda.api.dto.CreateOrderResDTO;
import cn.pumluda.api.response.Response;
import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.SkuEntity;
import cn.pumluda.domain.trade.service.createOrder.ICreateOrderService;
import cn.pumluda.infrastructure.redis.IdempotencyChecker;
import cn.pumluda.rateLimiter.annotations.AccessRateLimit;
import cn.pumluda.types.common.RedisKeyConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigInteger;

/**
 * Project: group-buy-market-better <p>
 * File: TradeController <p>
 * Created by: 16374 <p>
 * Date: 2026/5/26 <p>
 * Time: 17:55 <p>
 * Description: 交易业务 Controller
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/trade")
public class TradeController implements ITradeController {

    @Resource
    private IdempotencyChecker idempotencyChecker;
    @Resource
    private ICreateOrderService createOrderService;

    @PostMapping("create_order")
    @Override
    public Response<CreateOrderResDTO> createOrder(@RequestBody CreateOrderReqDTO requestDTO) {
        String userId = requestDTO.getUserId();
        BigInteger activityId = requestDTO.getActivityId();
        BigInteger groupTeamId = requestDTO.getGroupTeamId();
        BigInteger skuId = requestDTO.getSkuId();
        int entrySource = requestDTO.getEntrySource();
        String payChannel = requestDTO.getChannel();

        /* 1. 前置基础校验 */
        /* 幂等性校验：每个用户在每 5 秒内只能发起一次创建订单请求 */
        idempotencyChecker.tryAcquire(RedisKeyConstants.CREATE_ORDER, userId, 5);

        /* 商品库存与活动有效性校验 */
        SkuEntity sku = createOrderService.queryProductBySkuId(skuId);

        ActivityConfigEntity activityConfig = createOrderService.queryValidActivity(
                activityId);



        // 主动删除业务幂等号，避免阻塞下一个订单创建
        idempotencyChecker.release(RedisKeyConstants.CREATE_ORDER, userId);
        return null;
    }

    @AccessRateLimit(limitKey = "userId", qps = 1, fallback = "testFallback", blockThreshold = 1)
    @GetMapping("test")
    public String testLimit(@RequestParam(name = "userId") String userId) {
        return "test";
    }


    public String testFallback(String info) {
        return "limit";
    }
}
