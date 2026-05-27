package cn.pumluda.rateLimiter.config;

import cn.pumluda.rateLimiter.aop.RateLimiterAOP;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Project: group-buy-market-better <p>
 * File: RateLimiterConfig <p>
 * Created by: 16374 <p>
 * Date: 2026/5/27 <p>
 * Time: 15:31 <p>
 * Description: 启动入口
 */
@Configuration
public class RateLimiterConfig {
    @Bean
    public RateLimiterAOP rateLimiterAOP() {
        return new RateLimiterAOP();
    }
}

