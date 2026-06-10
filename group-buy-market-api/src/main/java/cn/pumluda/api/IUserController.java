package cn.pumluda.api;

import cn.pumluda.api.dto.UserOrderDetailReqDTO;
import cn.pumluda.api.dto.UserOrderDetailResDTO;
import cn.pumluda.api.dto.UserOrderListReqDTO;
import cn.pumluda.api.dto.UserOrderListResDTO;
import cn.pumluda.api.response.Response;

/**
 * Project: group-buy-market-better <p>
 * File: IUserController <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 用户中心 API
 */
public interface IUserController {

    /**
     * 用户订单列表（我的订单）
     */
    Response<UserOrderListResDTO> getUserOrderList(UserOrderListReqDTO requestDTO);

    /**
     * 用户订单详情
     */
    Response<UserOrderDetailResDTO> getUserOrderDetail(UserOrderDetailReqDTO requestDTO);

}
