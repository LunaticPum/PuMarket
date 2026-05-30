package cn.pumluda.domain.trade.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Project: group-buy-market-better <p>
 * File: OrderStatusEnumVo <p>
 * Created by: 16374 <p>
 * Date: 2026/5/27 <p>
 * Time: 09:54 <p>
 * Description: 订单状态枚举值对象
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum OrderStatusEnumVo {
    CREATE(0, "创建订单"), COMPLETE(1, "订单交易完成"), CLOSE(2, "超时关单");

    private int code;
    private String info;

    public static OrderStatusEnumVo getStatus(Integer code) {
        return switch (code) {
            case 1 -> COMPLETE;
            case 2 -> CLOSE;
            default -> CREATE;
        };
    }
}
