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
 * Description: 折扣优惠计算
 */
@Slf4j
@Service("percentOff")
public class PercentOffDiscountCalculator implements DiscountCalculator {
    private static final BigDecimal MIN_PAY_PRICE = new BigDecimal("0.01");

    @Override
    public BigDecimal[] calculate(BigDecimal originPrice, int quantity, Map<String, Object> config) {
        log.info(
                "[创建订单] 折扣优惠计算, originPrice:{}, quantity:{}, config:{}",
                originPrice,
                quantity,
                config
        );

        Object percentObj = config.get("percent");

        if (percentObj == null) {
            log.error("[创建订单] 缺少优惠配置信息");
            throw new AppException(
                    ResponseEnum.ILLEGAL_PARAMETER.getCode(),
                                   ResponseEnum.ILLEGAL_PARAMETER.getInfo()
            );
        }

        /* 防呆机制：防止配置人员误设置 */
        BigDecimal percent = new BigDecimal(percentObj.toString());
        if (percent.compareTo(BigDecimal.ZERO) <= 0 ||
            percent.compareTo(BigDecimal.valueOf(100)) >= 0) {
            throw new AppException(
                    ResponseEnum.ILLEGAL_PARAMETER.getCode(),
                                   "折扣比例必须在 0 ~ 100 之间"
            );
        }

        BigDecimal totalPrice = originPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal discountRate = percent.divide(
                BigDecimal.valueOf(100),
                4,
                java.math.RoundingMode.HALF_UP
        );

        /* 带小数金额保留两位小数，且四舍五入 */
        BigDecimal actualPrice = totalPrice.multiply(discountRate).setScale(
                2,
                java.math.RoundingMode.HALF_UP
        );

        /* 最低价格为 0.01 元 */
        if (actualPrice.compareTo(MIN_PAY_PRICE) < 0) {
            return new BigDecimal[]{totalPrice.subtract(actualPrice), MIN_PAY_PRICE};
        }

        return new BigDecimal[]{totalPrice.subtract(actualPrice), actualPrice};
    }
}
