package cn.pumluda.trigger.http;

import cn.pumluda.api.ITradeController;
import cn.pumluda.api.dto.CreateOrderReqDTO;
import cn.pumluda.api.dto.CreateOrderResDTO;
import cn.pumluda.api.response.Response;
import cn.pumluda.domain.trade.model.aggregate.BusinessAggregate;
import cn.pumluda.domain.trade.model.entity.OrderItemEntity;
import cn.pumluda.domain.trade.model.valobj.OrderStatusEnumVo;
import cn.pumluda.domain.trade.model.valobj.TradeSCVo;
import cn.pumluda.domain.trade.service.creatOrder.ICreateOrderService;
import cn.pumluda.domain.trade.service.preCheck.IPreCheckService;
import cn.pumluda.domain.trade.service.preCheck.dto.PreCheckResult;
import cn.pumluda.rateLimiter.annotations.AccessRateLimit;
import cn.pumluda.types.common.ActivityConstants;
import cn.pumluda.types.common.RedisConstants;
import cn.pumluda.types.enums.ActivityTypeEnum;
import cn.pumluda.types.enums.ResponseEnum;
import cn.pumluda.types.exception.AppException;
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

    @AccessRateLimit(limitKey = "userId", qps = 20, fallback = "testFallback", blockThreshold = 10)
    @PostMapping("create_order")
    @Override
    public Response<CreateOrderResDTO> createOrder(@RequestBody CreateOrderReqDTO requestDTO) {

        /* 1. 前置校验 */
        Long userId = requestDTO.getUserId();
        String orderNo = requestDTO.getOrderNo();
        Long activityId = requestDTO.getActivityId();
        Long skuId = requestDTO.getSkuId();
        int quantity = requestDTO.getQuantity();

        String bizId = userId + ":" + activityId + ":" + skuId; // 业务幂等号

        /* 非法参数校验 */
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
        Object preCheckResult = preCheckService.preCheck(
                userId,
                orderNo,
                activityId,
                skuId,
                quantity
        );  // preCheck 内部会重新拼接一次业务幂等号，分开来传是因为内部其他方法会用到

        if (preCheckResult instanceof ResponseEnum result) {
            return Response.<CreateOrderResDTO>builder()
                           .code(result.getCode())
                           .info(result.getInfo())
                           .build();
        }

        /* 2. 创建订单 */
        try {
            // 如果前置校验通过，则从校验结果获取事先查询到的数据，并与交易单号一起封装为业务数据聚合类
            BusinessAggregate businessAggregate;
            if (preCheckResult instanceof PreCheckResult result) {
                businessAggregate = BusinessAggregate.builder()
                                                     .userId(userId)
                                                     .orderNo(orderNo)
                                                     .groupTeamId(requestDTO.getGroupTeamId())
                                                     .sku(result.getSku())
                                                     .quantity(quantity)
                                                     .activityConfig(result.getActivityConfig())
                                                     .tradeSC(TradeSCVo.builder()
                                                                       .entrySource(requestDTO.getEntrySource())
                                                                       .build())
                                                     .build();
            } else {
                log.error("[创建订单] 前置校验结果获取异常");
                return Response.<CreateOrderResDTO>builder()
                               .code(ResponseEnum.ILLEGAL_PARAMETER.getCode())
                               .info(ResponseEnum.ILLEGAL_PARAMETER.getInfo())
                               .build();
            }

            OrderItemEntity orderItem = createOrderService.createOrder(businessAggregate);

            CreateOrderResDTO responseDto = CreateOrderResDTO.builder()
                                                             .orderNo(orderNo)
                                                             .payPrice(orderItem.getActualPrice())
                                                             .discountPrice(orderItem.getDiscountPrice())
                                                             .activityId(orderItem.getActivityId())
                                                             .activityType(ActivityTypeEnum.of(orderItem.getActivityType()))
                                                             .orderStatus(OrderStatusEnumVo.CREATE.getCode())
                                                             .build();

            // 记得释放幂等锁
            idempotencyChecker.release(RedisConstants.CREATE_ORDER, bizId);

            return Response.<CreateOrderResDTO>builder()
                           .code(ResponseEnum.SUCCESS.getCode())
                           .info(ResponseEnum.SUCCESS.getInfo())
                           .data(responseDto)
                           .build();

        } catch (AppException e) {
            log.error(
                    "[创建订单] 订单创建业务出现已知异常 userId={}, orderNo={}",
                    requestDTO.getUserId(),
                    requestDTO.getOrderNo(),
                    e
            );

            return Response.<CreateOrderResDTO>builder()
                           .code(e.getCode())
                           .info(e.getInfo())
                           .build();
        } catch (Exception e) {
            log.error(
                    "[创建订单] 订单创建业务出现未知异常 req={}, orderNo={}",
                    requestDTO.getUserId(),
                    requestDTO.getOrderNo(),
                    e
            );

            return Response.<CreateOrderResDTO>builder().code(ResponseEnum.UN_ERROR.getCode()).info(
                    ResponseEnum.UN_ERROR.getInfo()).build();
        }
    }

    @AccessRateLimit(limitKey = "userId", qps = 1, fallback = "testFallback", blockThreshold = 1)
    @GetMapping("test")
    public String testLimit(@RequestParam(name = "userId") String userId) {
        return "test";
    }


    public String testFallback(String info) {
        return "limit";
    }

    public Response<CreateOrderResDTO> testFallback(CreateOrderReqDTO requestDTO) {
        return Response.<CreateOrderResDTO>builder()
                       .info("检测到您当前的行为异常，请等候后续处理").build();
    }
}
