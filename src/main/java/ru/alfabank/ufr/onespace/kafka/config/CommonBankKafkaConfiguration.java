package ru.alfabank.ufr.onespace.kafka.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.alfabank.ufr.onespace.kafka.config.properties.CommonBankKafkaProperties;

import java.util.Map;

@Slf4j
@Configuration
@EnableKafka
@RequiredArgsConstructor
public class CommonBankKafkaConfiguration {

    @Qualifier("commonBankKafkaProperties")
    private final CommonBankKafkaProperties commonBankKafkaProperties;

    @Bean(name = "commonBankKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> commonBankKafkaListenerContainerFactory(
            @Qualifier("commonBankKafkaConsumerFactory") final ConsumerFactory<String, String> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean(name = "commonBankKafkaConsumerFactory")
    public ConsumerFactory<String, String> getConsumerFactory() {
        final Map<String, Object> props = commonBankKafkaProperties.getConsumerProperties();
        log.info("commonBankKafkaConsumerProperties: {}. props: {}", commonBankKafkaProperties, props);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean(name = "commonBankKafkaProducerFactory")
    public ProducerFactory<String, Object> commonBankKafkaProducerFactory() {
        final Map<String, Object> props = commonBankKafkaProperties.getProducerProperties();
        log.info("commonBankKafkaProducerProperties: {}. props: {}", commonBankKafkaProperties, props);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean(name = "commonBankKafkaTemplate")
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(commonBankKafkaProducerFactory());
    }
}
