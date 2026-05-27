package cn.pumluda.types.annotations;

import java.lang.annotation.*;

/**
 * Project: group-buy-market-pumluda <p>
 * File: DCCValue <p>
 * Created by: 16374 <p>
 * Date: 2026/5/16 <p>
 * Time: 16:38 <p>
 * Description: DCC注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface DCCValue {

    /* 默认配置字段值 */
    String value() default "";

}
