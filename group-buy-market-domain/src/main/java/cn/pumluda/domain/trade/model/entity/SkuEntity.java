package cn.pumluda.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Project: group-buy-market-better <p>
 * File: SkuEntity <p>
 * Created by: 16374 <p>
 * Date: 2026/5/27 <p>
 * Time: 10:18 <p>
 * Description: 商品 Sku 实体（RPC获取）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkuEntity {

    /* sku ID */
    private Long skuId;
    /* 商品名称 */
    private String productName;
    /* 商品主图URL */
    private String imageUrl;
    /* 商品描述 */
    private String description;
    /* 商品原始单价 */
    private BigDecimal price;
    /* 商品库存 */
    private Integer stock;
    /* 商品状态：0-下架，1-上架 */
    private Integer status;

}
