package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: ProductListResDTO <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Time: 14:00 <p>
 * Description: 商品列表响应 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductListResDTO {

    /* 商品总数 */
    private long total;

    /* 当前页码 */
    private int page;

    /* 每页条数 */
    private int size;

    /* 商品卡片列表 */
    private List<ProductCard> list;

    /**
     * 商品卡片（列表中的简要信息）
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductCard {

        /* 商品 SKU ID */
        private Long skuId;

        /* 商品名称 */
        private String productName;

        /* 商品主图 URL */
        private String imageUrl;

        /* 商品原价 */
        private BigDecimal originPrice;

        /* 最低拼团价（当前可用活动中最低的成团价格） */
        private BigDecimal minGroupPrice;

        /* 活动标签列表 */
        private List<ActivityTag> activityTags;

        /* 正在进行中的拼团队伍数 */
        private int activeGroupCount;

        /* 剩余库存 */
        private int stock;
    }

    /**
     * 活动标签（列表展示用）
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActivityTag {

        /* 活动 ID */
        private Long activityId;

        /* 活动名称 */
        private String activityName;

        /* 活动类型：0-默认，1-拼团，2-凑单 */
        private Integer activityType;
    }

}
