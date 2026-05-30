package cn.pumluda.trigger.http;

import cn.pumluda.api.ITradeController;
import cn.pumluda.api.dto.CreateOrderReqDTO;
import cn.pumluda.api.dto.CreateOrderResDTO;
import cn.pumluda.api.response.Response;
import cn.pumluda.domain.trade.model.aggregate.BusinessAggregate;
import cn.pumluda.domain.trade.model.entity.OrderItemEntity;
import cn.pumluda.domain.trade.model.valobj.TradeSCVo;
import cn.pumluda.domain.trade.service.creatOrder.ICreateOrderService;
import cn.pumluda.domain.trade.service.preCheck.IPreCheckService;
import cn.pumluda.domain.trade.service.preCheck.dto.PreCheckResult;
import cn.pumluda.rateLimiter.annotations.AccessRateLimit;
import cn.pumluda.types.common.ActivityConstants;
import cn.pumluda.types.common.RedisConstants;
import cn.pumluda.types.enums.ResponseEnum;
import cn.pumluda.types.utils.RedisIdempotencyChecker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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
    private IPreCheckService preCheckService;
    @Resource
    private ICreateOrderService createOrderService;
    @Resource
    private RedisIdempotencyChecker idempotencyChecker;

    @PostMapping("create_order")
    @Override
    public Response<CreateOrderResDTO> createOrder(@RequestBody CreateOrderReqDTO requestDTO) {
        /* 1. 前置校验 */
        Long userId = requestDTO.getUserId();
        String orderNo = requestDTO.getOrderNo();
        Long activityId = requestDTO.getActivityId();
        Long skuId = requestDTO.getSkuId();
        int quantity = requestDTO.getQuantity();

        // 非法参数校验
        if (null == userId || StringUtils.isBlank(orderNo) || null == skuId || quantity <= 0) {
            log.error("[创建订单] 请求参数非法，请检查");
            return Response.<CreateOrderResDTO>builder()
                           .code(ResponseEnum.ILLEGAL_PARAMETER.getCode())
                           .info(ResponseEnum.ILLEGAL_PARAMETER.getInfo())
                           .build();
        }

        // 如果是不参与活动的订单，则设置 activityId 为空活动占位值
        if (activityId == null) {
            activityId = ActivityConstants.EMPTY_ACTIVITY;
        }

        // 前置校验：如果校验结果类型是响应枚举类型，说明前置校验未通过，响应给客户端并告知校验失败原因
        Object preCheckResult = preCheckService.preCheck(userId, activityId, skuId, quantity);
        if (preCheckResult instanceof ResponseEnum result) {
            return Response.<CreateOrderResDTO>builder()
                           .code(result.getCode())
                           .info(result.getInfo())
                           .build();
        }

        /* 2. 创建订单 */
        // 如果前置校验通过，则从校验结果获取事先查询到的数据以及业务幂等号，并与交易单号一起封装为业务数据聚合类
        BusinessAggregate businessAggregate;
        String bizId;
        if (preCheckResult instanceof PreCheckResult result) {
            bizId = result.getBizId();
            businessAggregate = BusinessAggregate.builder()
                                                 .userId(userId)
                                                 .orderNo(orderNo)
                                                 .groupTeamId(requestDTO.getGroupTeamId())
                                                 .sku(result.getSku())
                                                 .quantity(quantity)
                                                 .activityConfig(result.getActivityConfig())
                                                 .tradeSC(TradeSCVo.builder()
                                                                   .entrySource(requestDTO.getEntrySource())
                                                                   .channel(requestDTO.getChannel())
                                                                   .build())
                                                 .bizId(bizId)
                                                 .build();
        } else {
            log.error("[创建订单] 前置校验结果获取异常");
            return Response.<CreateOrderResDTO>builder()
                           .code(ResponseEnum.ILLEGAL_PARAMETER.getCode())
                           .info(ResponseEnum.ILLEGAL_PARAMETER.getInfo())
                           .build();
        }

        try {
            OrderItemEntity orderItem = createOrderService.createOrder(businessAggregate);
        } catch (Exception e) {
            // todo 待完善的异常处理

            throw new RuntimeException(e);
        }

        // todo MQ 异步通知
        // 主动删除业务幂等号，避免阻塞下一个订单创建
        idempotencyChecker.release(RedisConstants.CREATE_ORDER, bizId);

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
