package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Project: group-buy-market-better <p>
 * File: ProductDetailReqDTO <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Time: 14:00 <p>
 * Description: 商品详情请求 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailReqDTO {

    /* 商品 SKU ID */
    private Long skuId;

    /* 用户 ID（可选：用于查询用户是否已参与该商品的拼团） */
    private Long userId;

}
