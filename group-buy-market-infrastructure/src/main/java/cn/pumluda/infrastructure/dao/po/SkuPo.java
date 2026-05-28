package cn.pumluda.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Project: group-buy-market-better <p>
 * File: SkuPo <p>
 * Created by: 16374 <p>
 * Date: 2026/5/28 <p>
 * Time: 09:03 <p>
 * Description: 商品 SKU PO
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkuPo {

    /* sku ID */
    private Long skuId;
    /* 商品名称 */
    private String productName;
    /* 商品原始单价 */
    private BigDecimal price;
    /* 商品库存 */
    private Integer stock;
    /* 预扣减库存：为已创建但未结算的订单留有的商品库存 */
    private Integer lockedStock;
    /* 商品状态：0-下架，1-上架 */
    private Integer status;

    /* 记录创建时间 */
    private Date createTime;
    /* 记录更新时间 */
    private Date updateTime;

}
