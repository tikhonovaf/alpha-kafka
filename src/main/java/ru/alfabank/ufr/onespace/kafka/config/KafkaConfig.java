package ru.alfabank.ufr.onespace.kafka.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import ru.alfabank.ufr.onespace.kafka.config.properties.DefaultKafkaProperties;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class KafkaConfig {

    @Qualifier("defaultKafkaProperties")
    private final DefaultKafkaProperties defaultKafkaProperties;

    @Bean
    @ConditionalOnProperty(name = "exponential-retry", havingValue = "true")
    public RetryTemplate exponentialRetryTemplate() {
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(10);

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2);

        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backOffPolicy);

        return template;
    }

    // Для тестов
    @Bean
    @ConditionalOnProperty(name = "exponential-retry", havingValue = "false")
    public RetryTemplate retryTemplate() {
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(5);

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(1000);

        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backOffPolicy);

        return template;
    }

    @Bean(name = "businessResultKafkaProducerFactory")
    public ProducerFactory<String, Object> businessResultKafkaProducerFactory() {
        final Map<String, Object> props = defaultKafkaProperties.getProducerProperties();
        log.info("defaultKafkaProducerProperties: {}. props: {}", defaultKafkaProperties, props);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean(name = "businessResultKafkaTemplate")
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(businessResultKafkaProducerFactory());
    }
}
