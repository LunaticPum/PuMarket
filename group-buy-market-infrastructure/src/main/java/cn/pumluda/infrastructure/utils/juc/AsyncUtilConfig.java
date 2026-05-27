package cn.pumluda.infrastructure.utils.juc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Project: group-buy-market-better <p>
 * File: AsyncUtilConfig <p>
 * Created by: 16374 <p>
 * Date: 2026/5/27 <p>
 * Time: 18:00 <p>
 * Description: 并发工具配置类
 */
@Configuration
public class AsyncUtilConfig {

    @Bean
    public CompletableFutureUtils completableFutureUtils(ThreadPoolExecutor threadPoolExecutor) {
        return new CompletableFutureUtils(threadPoolExecutor);
    }
}
