package ru.alfabank.ufr.onespace.kafka.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import ru.alfabank.ufr.onespace.kafka.model.MlmDto;

@Service
@Slf4j
public class MlmInfoKafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RetryTemplate retryTemplate;

    @Value("${spring.mlm-kafka.topic.name}")
    private String topicName;

    public MlmInfoKafkaProducerService(
        @Qualifier("mlmKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate,
        RetryTemplate retryTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.retryTemplate = retryTemplate;
    }

    public void sendMessage(MlmDto message) {
        final String messageId = message.UCID();
        final String key = message.operatorId();
        log.info("Sending message, UCID: {}, key: {}", messageId, key);
        retryTemplate.execute(context -> {
            try {
                log.info("Attempt number: {} for UCID: {}, key: {}", context.getRetryCount() + 1, messageId, key);
                kafkaTemplate.send(topicName, key, message).addCallback(
                    result -> log.info("Message sent successfully, UCID: {}, key: {}", messageId, key),
                    ex -> log.error("Failed to send message, UCID: {}, key: {}", messageId, key, ex)
                );
            } catch (Exception e) {
                log.error("An exception occurred during sending message, UCID: {}, key: {}", messageId, key, e);
                throw e;
            }
            return null;
        });
    }
}
