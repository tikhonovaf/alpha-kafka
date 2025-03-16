package ru.alfabank.ufr.onespace.kafka.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import ru.alfabank.ufr.onespace.kafka.model.CallDetailsDto;

@Service
@Slf4j
public class CallDetailsKafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RetryTemplate retryTemplate;

    @Value("${spring.kafka.topic.name}")
    private String topicName;

    public CallDetailsKafkaProducerService(
        @Qualifier("businessResultKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate,
        RetryTemplate retryTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.retryTemplate = retryTemplate;
    }

    public void sendMessage(CallDetailsDto message) {
        String messageId = message.getPomSessionId();
        log.info("Sending message, pomSessionId: {}", messageId);
        retryTemplate.execute(context -> {
            try {
                log.info("Attempt number: {} for pomSessionId: {}", context.getRetryCount() + 1, messageId);
                kafkaTemplate.send(topicName, message).addCallback(
                    result -> log.info("Message sent successfully, pomSessionId: {}", messageId),
                    ex -> log.error("Failed to send message, pomSessionId: {}", messageId, ex)
                );
            } catch (Exception e) {
                log.error("An exception occurred during sending message, pomSessionId: {}", messageId, e);
                throw e;
            }
            return null;
        });
    }
}
