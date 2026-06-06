package cn.pumluda.trigger.http;

import cn.pumluda.api.IUserController;
import cn.pumluda.api.dto.UserOrderDetailReqDTO;
import cn.pumluda.api.dto.UserOrderDetailResDTO;
import cn.pumluda.api.dto.UserOrderListReqDTO;
import cn.pumluda.api.dto.UserOrderListResDTO;
import cn.pumluda.api.response.Response;
import cn.pumluda.domain.trade.model.entity.SkuEntity;
import cn.pumluda.domain.trade.service.productQuery.ProductQueryService;
import cn.pumluda.domain.trade.service.userQuery.UserOrderDetailResult;
import cn.pumluda.domain.trade.service.userQuery.UserOrderItem;
import cn.pumluda.domain.trade.service.userQuery.UserOrderListResult;
import cn.pumluda.domain.trade.service.userQuery.UserOrderQueryService;
import cn.pumluda.types.enums.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: UserController <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 用户中心 Controller
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/user")
public class UserController implements IUserController {

    @Resource
    private UserOrderQueryService userOrderQueryService;

    @Resource
    private ProductQueryService productQueryService;

    @PostMapping("orders")
    @Override
    public Response<UserOrderListResDTO> getUserOrderList(@RequestBody UserOrderListReqDTO requestDTO) {
        try {
            Long userId = requestDTO.getUserId();
            if (userId == null) {
                return Response.<UserOrderListResDTO>builder()
                        .code(ResponseEnum.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseEnum.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            int page = requestDTO.getPage() != null && requestDTO.getPage() > 0 ? requestDTO.getPage() : 1;
            int size = requestDTO.getSize() != null && requestDTO.getSize() > 0 ? requestDTO.getSize() : 10;

            UserOrderListResult result = userOrderQueryService.getUserOrderList(userId, page, size);

            List<UserOrderListResDTO.OrderCard> cards = new ArrayList<>();
            for (UserOrderItem item : result.getItems()) {
                // 从缓存获取商品图片
                String imageUrl = null;
                if (item.getSkuId() != null) {
                    SkuEntity sku = productQueryService.getProductDetail(item.getSkuId());
                    if (sku != null) {
                        imageUrl = sku.getImageUrl();
                    }
                }

                cards.add(UserOrderListResDTO.OrderCard.builder()
                        .orderNo(item.getOrderNo())
                        .productName(item.getProductName())
                        .imageUrl(imageUrl)
                        .originPrice(item.getOriginPrice())
                        .actualPrice(item.getActualPrice())
                        .quantity(item.getQuantity())
                        .orderStatus(item.getOrderStatus())
                        .displayStatus(item.getDisplayStatus())
                        .activityName(item.getActivityName())
                        .groupTeamId(item.getGroupTeamId())
                        .teamStatus(item.getTeamStatus())
                        .teamProgress(item.getTeamProgress())
                        .orderCreateTime(item.getOrderCreateTime())
                        .build());
            }

            return Response.<UserOrderListResDTO>builder()
                    .code(ResponseEnum.SUCCESS.getCode())
                    .info(ResponseEnum.SUCCESS.getInfo())
                    .data(UserOrderListResDTO.builder()
                            .total(result.getTotal())
                            .page(result.getPage())
                            .size(result.getSize())
                            .list(cards)
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("[用户订单列表] 查询异常", e);
            return Response.<UserOrderListResDTO>builder()
                    .code(ResponseEnum.UN_ERROR.getCode())
                    .info(ResponseEnum.UN_ERROR.getInfo())
                    .build();
        }
    }

    @PostMapping("order/detail")
    @Override
    public Response<UserOrderDetailResDTO> getUserOrderDetail(@RequestBody UserOrderDetailReqDTO requestDTO) {
        try {
            String orderNo = requestDTO.getOrderNo();
            Long userId = requestDTO.getUserId();

            if (orderNo == null || orderNo.isBlank() || userId == null) {
                return Response.<UserOrderDetailResDTO>builder()
                        .code(ResponseEnum.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseEnum.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            UserOrderDetailResult result = userOrderQueryService.getUserOrderDetail(orderNo, userId);
            if (result == null) {
                return Response.<UserOrderDetailResDTO>builder()
                        .code(ResponseEnum.ORDER_NOT_FOUND.getCode())
                        .info(ResponseEnum.ORDER_NOT_FOUND.getInfo())
                        .build();
            }

            // 获取商品图片
            String imageUrl = null;
            if (result.getSkuId() != null) {
                SkuEntity sku = productQueryService.getProductDetail(result.getSkuId());
                if (sku != null) {
                    imageUrl = sku.getImageUrl();
                }
            }

            UserOrderDetailResDTO.TeamInfo teamInfo = null;
            if (result.getTeamInfo() != null) {
                UserOrderDetailResult.TeamInfo t = result.getTeamInfo();
                teamInfo = UserOrderDetailResDTO.TeamInfo.builder()
                        .groupTeamId(t.getGroupTeamId())
                        .leaderUserId(t.getLeaderUserId())
                        .requiredNum(t.getRequiredNum())
                        .currentNum(t.getCurrentNum())
                        .settledTradeNum(t.getSettledTradeNum())
                        .teamStatus(t.getTeamStatus())
                        .teamStatusName(t.getTeamStatusName())
                        .remainingSlots(t.getRemainingSlots())
                        .teamCreateTime(t.getTeamCreateTime())
                        .teamExpireTime(t.getTeamExpireTime())
                        .build();
            }

            return Response.<UserOrderDetailResDTO>builder()
                    .code(ResponseEnum.SUCCESS.getCode())
                    .info(ResponseEnum.SUCCESS.getInfo())
                    .data(UserOrderDetailResDTO.builder()
                            .orderNo(result.getOrderNo())
                            .userId(result.getUserId())
                            .productName(result.getProductName())
                            .imageUrl(imageUrl)
                            .originPrice(result.getOriginPrice())
                            .actualPrice(result.getActualPrice())
                            .discountPrice(result.getDiscountPrice())
                            .quantity(result.getQuantity())
                            .orderStatus(result.getOrderStatus())
                            .displayStatus(result.getDisplayStatus())
                            .orderCreateTime(result.getOrderCreateTime())
                            .orderExpireTime(result.getOrderExpireTime())
                            .activityId(result.getActivityId())
                            .activityName(result.getActivityName())
                            .activityType(result.getActivityType())
                            .teamInfo(teamInfo)
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("[用户订单详情] 查询异常", e);
            return Response.<UserOrderDetailResDTO>builder()
                    .code(ResponseEnum.UN_ERROR.getCode())
                    .info(ResponseEnum.UN_ERROR.getInfo())
                    .build();
        }
    }
}
