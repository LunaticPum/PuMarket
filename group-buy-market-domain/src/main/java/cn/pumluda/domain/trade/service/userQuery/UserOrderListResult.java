package cn.pumluda.domain.trade.service.userQuery;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Project: group-buy-market-better <p>
 * File: UserOrderListResult <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 用户订单列表查询结果
 */
@Getter
@AllArgsConstructor
public class UserOrderListResult {

    private final int total;
    private final int page;
    private final int size;
    private final List<UserOrderItem> items;

}
