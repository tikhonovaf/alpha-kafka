package ru.alfabank.ufr.onespace.kafka.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration("mlmKafkaProperties")
@ConfigurationProperties(prefix = "spring.mlm-kafka")
public class MlmKafkaProperties extends KafkaProperties {

}
