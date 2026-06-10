package cn.pumluda.api;

import cn.pumluda.api.dto.CreateOrderReqDTO;
import cn.pumluda.api.dto.CreateOrderResDTO;
import cn.pumluda.api.dto.SettleOrderReqDTO;
import cn.pumluda.api.dto.SettleOrderResDTO;
import cn.pumluda.api.response.Response;

/**
 * Project: group-buy-market-better <p>
 * File: ICreateOrderController <p>
 * Created by: 16374 <p>
 * Date: 2026/5/26 <p>
 * Time: 17:26 <p>
 * Description: 交易业务 API
 */
public interface ITradeController {

    Response<CreateOrderResDTO> createOrder(CreateOrderReqDTO requestDTO);

    Response<SettleOrderResDTO> settleOrder(SettleOrderReqDTO requestDTO);

    Response<SettleOrderResDTO> cancelOrder(SettleOrderReqDTO requestDTO);

}
