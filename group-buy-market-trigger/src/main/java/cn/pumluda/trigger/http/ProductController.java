package cn.pumluda.trigger.http;

import cn.pumluda.api.IProductController;
import cn.pumluda.api.dto.ProductDetailReqDTO;
import cn.pumluda.api.dto.ProductDetailResDTO;
import cn.pumluda.api.dto.ProductListReqDTO;
import cn.pumluda.api.dto.ProductListResDTO;
import cn.pumluda.api.response.Response;
import cn.pumluda.domain.trade.model.entity.ActivityConfigEntity;
import cn.pumluda.domain.trade.model.entity.GroupTeamEntity;
import cn.pumluda.domain.trade.model.entity.SkuEntity;
import cn.pumluda.domain.trade.service.activityManage.ActivityManageService;
import cn.pumluda.domain.trade.service.productQuery.ProductQueryService;
import cn.pumluda.types.enums.ActivityTypeEnum;
import cn.pumluda.types.enums.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Project: group-buy-market-better <p>
 * File: ProductController <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Time: 14:00 <p>
 * Description: 商品浏览 Controller
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/product")
public class ProductController implements IProductController {

    @Resource
    private ProductQueryService productQueryService;

    @Resource
    private ActivityManageService activityManageService;

    @PostMapping("list")
    @Override
    public Response<ProductListResDTO> getProductList(@RequestBody ProductListReqDTO requestDTO) {
        try {
            int page = requestDTO.getPage() != null && requestDTO.getPage() > 0 ? requestDTO.getPage() : 1;
            int size = requestDTO.getSize() != null && requestDTO.getSize() > 0 ? requestDTO.getSize() : 10;

            // 1. 分页查商品
            List<SkuEntity> skus = productQueryService.getProductList(page, size);
            int total = productQueryService.countProducts();

            // 2. 组装商品卡片列表（每个商品只查其绑定的单个活动）
            List<ProductListResDTO.ProductCard> cards = new ArrayList<>();
            for (SkuEntity sku : skus) {
                // 获取该商品当前绑定的活动
                ActivityConfigEntity boundActivity = activityManageService.getActiveActivityForSku(sku.getSkuId());

                List<ProductListResDTO.ActivityTag> tags = new ArrayList<>();
                BigDecimal minGroupPrice = null;

                if (boundActivity != null && boundActivity.getActivityType() != null
                        && boundActivity.getActivityType() != 0) {
                    tags.add(ProductListResDTO.ActivityTag.builder()
                            .activityId(boundActivity.getActivityId())
                            .activityName(boundActivity.getActivityName())
                            .activityType(boundActivity.getActivityType())
                            .build());

                    BigDecimal groupPrice = calcGroupPrice(sku.getPrice(), boundActivity);
                    minGroupPrice = groupPrice;
                }

                // 查该商品的进行中队伍数
                int activeGroupCount = productQueryService.countActiveTeams(sku.getSkuId());

                cards.add(ProductListResDTO.ProductCard.builder()
                        .skuId(sku.getSkuId())
                        .productName(sku.getProductName())
                        .imageUrl(sku.getImageUrl())
                        .originPrice(sku.getPrice())
                        .minGroupPrice(minGroupPrice)
                        .activityTags(tags)
                        .activeGroupCount(activeGroupCount)
                        .stock(sku.getStock())
                        .build());
            }

            return Response.<ProductListResDTO>builder()
                    .code(ResponseEnum.SUCCESS.getCode())
                    .info(ResponseEnum.SUCCESS.getInfo())
                    .data(ProductListResDTO.builder()
                            .total(total)
                            .page(page)
                            .size(size)
                            .list(cards)
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("[商品列表] 查询异常", e);
            return Response.<ProductListResDTO>builder()
                    .code(ResponseEnum.UN_ERROR.getCode())
                    .info(ResponseEnum.UN_ERROR.getInfo())
                    .build();
        }
    }

    @PostMapping("detail")
    @Override
    public Response<ProductDetailResDTO> getProductDetail(@RequestBody ProductDetailReqDTO requestDTO) {
        try {
            Long skuId = requestDTO.getSkuId();
            if (skuId == null) {
                return Response.<ProductDetailResDTO>builder()
                        .code(ResponseEnum.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseEnum.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            // 1. 查商品
            SkuEntity sku = productQueryService.getProductDetail(skuId);
            if (sku == null) {
                return Response.<ProductDetailResDTO>builder()
                        .code(ResponseEnum.SKU_NULL.getCode())
                        .info(ResponseEnum.SKU_NULL.getInfo())
                        .build();
            }

            // 2. 查该商品绑定的活动
            ActivityConfigEntity boundActivity = activityManageService.getActiveActivityForSku(skuId);
            List<ProductDetailResDTO.ActivityDetail> activityDetails = new ArrayList<>();
            if (boundActivity != null) {
                ActivityConfigEntity activity = boundActivity;
                String discountDesc = buildDiscountDesc(activity);
                BigDecimal groupPrice = calcGroupPrice(sku.getPrice(), activity);

                activityDetails.add(ProductDetailResDTO.ActivityDetail.builder()
                        .activityId(activity.getActivityId())
                        .activityName(activity.getActivityName())
                        .activityType(activity.getActivityType())
                        .activityTypeName(ActivityTypeEnum.of(activity.getActivityType()).getName())
                        .discountType(activity.getDiscountType() != null ? activity.getDiscountType().getCode() : 0)
                        .discountTypeName(activity.getDiscountType() != null ? activity.getDiscountType().getName() : "none")
                        .discountDesc(discountDesc + (groupPrice != null ? "，拼团价 ¥" + groupPrice : ""))
                        .requiredNum(activity.getTotalDiscountQuota())
                        .quotaRemaining(activity.getTotalDiscountQuota() != null && activity.getUsedDiscountQuota() != null
                                ? activity.getTotalDiscountQuota() - activity.getUsedDiscountQuota() : null)
                        .startTime(activity.getStartTime())
                        .endTime(activity.getEndTime())
                        .build());
            }

            // 3. 查进行中的拼团队伍（只查该商品绑定的活动下的队伍）
            List<GroupTeamEntity> activeTeams = productQueryService.getActiveTeams(skuId);
            List<ProductDetailResDTO.TeamCard> teamCards = new ArrayList<>();
            for (GroupTeamEntity team : activeTeams) {
                teamCards.add(ProductDetailResDTO.TeamCard.builder()
                        .activityId(team.getActivityId())
                        .groupTeamId(team.getGroupTeamId())
                        .leaderUserId(team.getLeaderUserId())
                        .requiredNum(team.getRequiredNum())
                        .currentNum(team.getCurrentNum())
                        .remainingSlots(team.getRequiredNum() - team.getCurrentNum())
                        .expireTime(team.getTeamExpireTime())
                        .build());
            }

            // 4. 查已售数量
            int soldCount = productQueryService.getSoldCount(skuId);

            return Response.<ProductDetailResDTO>builder()
                    .code(ResponseEnum.SUCCESS.getCode())
                    .info(ResponseEnum.SUCCESS.getInfo())
                    .data(ProductDetailResDTO.builder()
                            .skuId(sku.getSkuId())
                            .productName(sku.getProductName())
                            .imageUrl(sku.getImageUrl())
                            .description(sku.getDescription())
                            .originPrice(sku.getPrice())
                            .stock(sku.getStock())
                            .soldCount(soldCount)
                            .status(sku.getStatus())
                            .activities(activityDetails)
                            .activeTeams(teamCards)
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("[商品详情] 查询异常", e);
            return Response.<ProductDetailResDTO>builder()
                    .code(ResponseEnum.UN_ERROR.getCode())
                    .info(ResponseEnum.UN_ERROR.getInfo())
                    .build();
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 根据活动优惠配置计算拼团价格
     */
    private BigDecimal calcGroupPrice(BigDecimal originPrice, ActivityConfigEntity activity) {
        if (originPrice == null || activity.getDiscountType() == null) {
            return null;
        }
        Map<String, Object> config = activity.getDiscountConfig();
        if (config == null) {
            return null;
        }

        try {
            return switch (activity.getDiscountType()) {
                case DIRECT_MINUS -> {
                    Object amount = config.get("amount");
                    if (amount instanceof Number num) {
                        yield originPrice.subtract(BigDecimal.valueOf(num.doubleValue()));
                    }
                    yield null;
                }
                case DIRECT_PRICE -> {
                    Object price = config.get("price");
                    if (price instanceof Number num) {
                        yield BigDecimal.valueOf(num.doubleValue());
                    }
                    yield null;
                }
                case PERCENT_OFF -> {
                    Object percent = config.get("percent");
                    if (percent instanceof Number num) {
                        yield originPrice.multiply(BigDecimal.valueOf(num.doubleValue()))
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    }
                    yield null;
                }
                case FULL_REDUCTION -> {
                    Object threshold = config.get("threshold");
                    Object amount = config.get("amount");
                    if (threshold instanceof Number t && amount instanceof Number a) {
                        if (originPrice.compareTo(BigDecimal.valueOf(t.doubleValue())) >= 0) {
                            yield originPrice.subtract(BigDecimal.valueOf(a.doubleValue()));
                        }
                    }
                    yield originPrice; // 不满足满减门槛，返回原价
                }
                default -> null;
            };
        } catch (Exception e) {
            log.warn("[商品价格计算] 计算拼团价失败 activityId={}", activity.getActivityId(), e);
            return null;
        }
    }

    /**
     * 构建优惠描述文本
     */
    private String buildDiscountDesc(ActivityConfigEntity activity) {
        if (activity.getDiscountType() == null) {
            return "暂无优惠";
        }
        Map<String, Object> config = activity.getDiscountConfig();

        return switch (activity.getDiscountType()) {
            case DIRECT_MINUS -> {
                Object amount = config != null ? config.get("amount") : null;
                yield "立减" + (amount instanceof Number num ? num.intValue() : "?") + "元";
            }
            case DIRECT_PRICE -> {
                Object price = config != null ? config.get("price") : null;
                yield "直降至" + (price instanceof Number num ? num.intValue() : "?") + "元";
            }
            case PERCENT_OFF -> {
                Object percent = config != null ? config.get("percent") : null;
                yield (percent instanceof Number num ? num.intValue() : "?") + "折优惠";
            }
            case FULL_REDUCTION -> {
                Object threshold = config != null ? config.get("threshold") : null;
                Object amount = config != null ? config.get("amount") : null;
                yield "满" + (threshold instanceof Number t ? t.intValue() : "?")
                        + "减" + (amount instanceof Number a ? a.intValue() : "?") + "元";
            }
            default -> "暂无优惠";
        };
    }
}
