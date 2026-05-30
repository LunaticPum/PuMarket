package cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.discountCalcStrategy.impl;

import cn.pumluda.domain.trade.service.creatOrder.ruleTreeImpl.discountCalcStrategy.DiscountCalculator;
import cn.pumluda.types.enums.ResponseEnum;
import cn.pumluda.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Project: group-buy-market-better <p>
 * File: MJCalculateService <p>
 * Created by: 16374 <p>
 * Date: 2026/5/30 <p>
 * Time: 09:11 <p>
 * Description: 直减优惠计算
 */
@Slf4j
@Service("directMinus")
public class DirectMinusDiscountCalculator implements DiscountCalculator {

    private static final BigDecimal MIN_PAY_PRICE = new BigDecimal("0.01");

    @Override
    public BigDecimal[] calculate(BigDecimal originPrice, int quantity, Map<String, Object> config) {
        log.info(
                "[创建订单] 直减优惠计算, originPrice:{}, quantity:{}, config:{}",
                originPrice,
                quantity,
                config
        );

        Object amountObj = config.get("amount");
        if (amountObj == null) {
            log.error("[创建订单] 缺少优惠配置信息");
            throw new AppException(
                    ResponseEnum.ILLEGAL_PARAMETER.getCode(),
                    ResponseEnum.ILLEGAL_PARAMETER.getInfo()
            );
        }

        /* 防呆机制：防止配置人员误设置 */
        BigDecimal amount = new BigDecimal(amountObj.toString());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(
                    ResponseEnum.ILLEGAL_PARAMETER.getCode(),
                    "优惠配置金额必须大于0"
            );
        }

        BigDecimal actualPrice = originPrice.multiply(BigDecimal.valueOf(quantity))
                                         .subtract(amount);

        /* 最低价格为 0.01 元 */
        if (actualPrice.compareTo(MIN_PAY_PRICE) < 0) {
            return new BigDecimal[]{amount, MIN_PAY_PRICE};
        }

        return new BigDecimal[]{amount, actualPrice};
    }
}
