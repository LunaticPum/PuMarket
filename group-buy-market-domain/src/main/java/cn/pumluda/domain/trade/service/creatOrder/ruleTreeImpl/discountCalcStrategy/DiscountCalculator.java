package cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.discountCalcStrategy;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Project: group-buy-market-better <p>
 * File: DiscountCalculator <p>
 * Created by: 16374 <p>
 * Date: 2026/5/30 <p>
 * Time: 09:06 <p>
 * Description: 优惠计算接口
 */
public interface DiscountCalculator {

    /**
     * 优惠计算器，计算优惠金额以及优惠后的支付金额
     * @param originPrice 商品原始单价
     * @param quantity 商品购买数量
     * @param config 优惠配置
     * @return [优惠金额，实际金额]
     */
    BigDecimal[] calculate(BigDecimal originPrice, int quantity, Map<String, Object> config);

}
