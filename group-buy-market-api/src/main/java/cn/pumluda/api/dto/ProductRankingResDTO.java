package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: ProductRankingResDTO <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 商品销售排行响应 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRankingResDTO {

    private List<RankItem> list;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RankItem {

        /* 排名 */
        private int rank;

        /* SKU ID */
        private Long skuId;

        /* 商品名称 */
        private String productName;

        /* 销售数量 */
        private int soldCount;

        /* 销售总额 */
        private BigDecimal totalAmount;
    }

}
