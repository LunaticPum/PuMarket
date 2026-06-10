package cn.pumluda.config;

import cn.pumluda.infrastructure.auth.MarketingAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Project: group-buy-market-better <p>
 * File: WebMvcConfig <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: Web MVC 配置 —— 注册营销后台鉴权拦截器
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final MarketingAuthInterceptor marketingAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(marketingAuthInterceptor)
                .addPathPatterns("/api/v1/marketing/**")
                .excludePathPatterns("/api/v1/marketing/login");
    }

}
