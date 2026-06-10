package cn.pumluda.api;

import cn.pumluda.api.dto.AiQueryReqDTO;
import cn.pumluda.api.dto.AiQueryResDTO;
import cn.pumluda.api.response.Response;

public interface IAiController {
    Response<AiQueryResDTO> queryData(AiQueryReqDTO req);
}
