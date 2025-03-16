package ru.alfabank.ufr.onespace.kafka.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration("commonBankKafkaProperties")
@ConfigurationProperties(prefix = "spring.common-bank-kafka")
public class CommonBankKafkaProperties extends KafkaProperties {

}
