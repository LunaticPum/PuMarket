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
 * Description: n元秒杀优惠计算
 */
@Slf4j
@Service("directPrice")
public class DirectPriceDiscountCalculator implements DiscountCalculator {

    @Override
    public BigDecimal[] calculate(BigDecimal originPrice, int quantity, Map<String, Object> config) {
        log.info(
                "[创建订单] 秒杀优惠计算, originPrice:{}, quantity:{}, config:{}",
                originPrice,
                quantity,
                config
        );

        Object priceObj = config.get("price");
        if (priceObj == null) {
            log.error("[创建订单] 缺少优惠配置信息");
            throw new AppException(
                    ResponseEnum.ILLEGAL_PARAMETER.getCode(),
                    ResponseEnum.ILLEGAL_PARAMETER.getInfo()
            );
        }

        /* 防呆机制：防止配置人员误设置 */
        BigDecimal actualPrice = new BigDecimal(priceObj.toString());
        if (actualPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(
                    ResponseEnum.ILLEGAL_PARAMETER.getCode(),
                    "优惠配置金额必须大于0"
            );
        }

        BigDecimal discountPrice = originPrice.multiply(BigDecimal.valueOf(quantity)).subtract(actualPrice);

        return new BigDecimal[]{discountPrice, actualPrice};
    }
}
