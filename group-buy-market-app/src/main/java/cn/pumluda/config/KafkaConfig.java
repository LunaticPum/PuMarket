package cn.pumluda.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Project: group-buy-market-better <p>
 * File: KafkaConfig <p>
 * Created by: 16374 <p>
 * Date: 2026/6/2 <p>
 * Time: 08:10 <p>
 * Description: Kafka配置类
 */
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic tradeTopic() {
        return TopicBuilder
                .name("trade-order-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

}


