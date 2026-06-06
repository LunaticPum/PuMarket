package cn.pumluda.domain.trade.service.marketing;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Project: group-buy-market-better <p>
 * File: ProductRankingItem <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 商品销售排行条目
 */
@Getter
@AllArgsConstructor
public class ProductRankingItem {

    private final int rank;
    private final Long skuId;
    private final String productName;
    private final int soldCount;
    private final BigDecimal totalAmount;

}
