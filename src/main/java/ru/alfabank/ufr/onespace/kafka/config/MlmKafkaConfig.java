package ru.alfabank.ufr.onespace.kafka.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.alfabank.ufr.onespace.kafka.config.properties.MlmKafkaProperties;

import java.util.Map;

@Slf4j
@Configuration
@EnableKafka
@RequiredArgsConstructor
public class MlmKafkaConfig {

    @Qualifier("mlmKafkaProperties")
    private final MlmKafkaProperties mlmKafkaProperties;

    @Bean(name = "mlmKafkaProducerFactory")
    public ProducerFactory<String, Object> mlmKafkaProducerFactory() {
        final Map<String, Object> props = mlmKafkaProperties.getProducerProperties();
        log.info("mlmKafkaProducerProperties: {}. props: {}", mlmKafkaProperties, props);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean(name = "mlmKafkaTemplate")
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(mlmKafkaProducerFactory());
    }
}
