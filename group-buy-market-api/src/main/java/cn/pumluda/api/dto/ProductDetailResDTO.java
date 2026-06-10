package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: ProductDetailResDTO <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Time: 14:00 <p>
 * Description: 商品详情响应 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailResDTO {

    // ==================== 商品基本信息 ====================

    /* 商品 SKU ID */
    private Long skuId;

    /* 商品名称 */
    private String productName;

    /* 商品主图 URL */
    private String imageUrl;

    /* 商品描述 */
    private String description;

    /* 商品原价 */
    private BigDecimal originPrice;

    /* 剩余库存 */
    private int stock;

    /* 历史已售数量 */
    private int soldCount;

    /* 商品状态：0-下架，1-上架 */
    private int status;

    // ==================== 可参与的活动列表 ====================

    private List<ActivityDetail> activities;

    // ==================== 进行中的拼团队伍 ====================

    private List<TeamCard> activeTeams;

    /**
     * 活动详情
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActivityDetail {

        /* 活动 ID */
        private Long activityId;

        /* 活动名称 */
        private String activityName;

        /* 活动类型：0-默认，1-拼团，2-凑单 */
        private Integer activityType;

        /* 活动类型名称 */
        private String activityTypeName;

        /* 优惠类型：0-无，1-直减，2-直降，3-折扣，4-满减 */
        private Integer discountType;

        /* 优惠类型名称 */
        private String discountTypeName;

        /* 优惠描述（前端友好展示，如"立减500元"/"8折优惠"） */
        private String discountDesc;

        /* 成团所需人数（拼团活动专用） */
        private Integer requiredNum;

        /* 活动剩余优惠名额（非拼团活动） */
        private Integer quotaRemaining;

        /* 活动开始时间 */
        private Date startTime;

        /* 活动结束时间 */
        private Date endTime;
    }

    /**
     * 拼团队伍卡片
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TeamCard {

        /* 活动 ID */
        private Long activityId;

        /* 拼团队伍 ID */
        private Long groupTeamId;

        /* 团长用户 ID */
        private Long leaderUserId;

        /* 成团所需人数 */
        private int requiredNum;

        /* 当前参团人数 */
        private int currentNum;

        /* 剩余可加入人数 */
        private int remainingSlots;

        /* 队伍有效截止时间 */
        private Date expireTime;
    }

}
