package ru.alfabank.ufr.onespace.kafka.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration("defaultKafkaProperties")
@ConfigurationProperties(prefix = "spring.kafka")
public class DefaultKafkaProperties extends KafkaProperties {

}
