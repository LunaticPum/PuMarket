package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Project: group-buy-market-better <p>
 * File: ProductRankingReqDTO <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 商品销售排行请求 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRankingReqDTO {

    /* 返回条数，默认 10 */
    private Integer limit;

}
