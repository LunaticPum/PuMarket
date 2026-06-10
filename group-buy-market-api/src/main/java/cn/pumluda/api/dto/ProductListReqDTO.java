package cn.pumluda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Project: group-buy-market-better <p>
 * File: ProductListReqDTO <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Time: 14:00 <p>
 * Description: 商品列表请求 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductListReqDTO {

    /* 页码，从 1 开始，默认 1 */
    private Integer page;

    /* 每页条数，默认 10 */
    private Integer size;

}
