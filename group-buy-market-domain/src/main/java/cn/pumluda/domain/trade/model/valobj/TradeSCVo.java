package cn.pumluda.domain.trade.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Project: group-buy-market-better <p>
 * File: TradeSCEnumVo <p>
 * Created by: 16374 <p>
 * Date: 2026/5/27 <p>
 * Time: 09:51 <p>
 * Description: 交易的流量入口和支付渠道
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeSCVo {
    private int entrySource;
    private String channel;
}
