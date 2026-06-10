package cn.pumluda.api;

import cn.pumluda.api.dto.ProductDetailReqDTO;
import cn.pumluda.api.dto.ProductDetailResDTO;
import cn.pumluda.api.dto.ProductListReqDTO;
import cn.pumluda.api.dto.ProductListResDTO;
import cn.pumluda.api.response.Response;

/**
 * Project: group-buy-market-better <p>
 * File: IProductController <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Time: 14:00 <p>
 * Description: 商品浏览业务 API
 */
public interface IProductController {

    /**
     * 商品列表（首页）
     */
    Response<ProductListResDTO> getProductList(ProductListReqDTO requestDTO);

    /**
     * 商品详情（详情页）
     */
    Response<ProductDetailResDTO> getProductDetail(ProductDetailReqDTO requestDTO);

}
