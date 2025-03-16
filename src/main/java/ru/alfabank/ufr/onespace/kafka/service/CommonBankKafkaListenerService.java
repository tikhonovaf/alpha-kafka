package ru.alfabank.ufr.onespace.kafka.service;

import static ru.alfabank.ufr.onespace.kafka.model.VoeSurveyDto.ACTION_CREATE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Service;
import ru.alfabank.ufr.onespace.kafka.model.VoeSurveyDto;

import java.util.Optional;

@Service
@Slf4j
@ConditionalOnProperty(name = "spring.common-bank-kafka.enable-consumer", havingValue = "true")
public class CommonBankKafkaListenerService {

    private final HazelcastService hazelcastService;
    private final ObjectMapper objectMapper;

    public CommonBankKafkaListenerService(
            KafkaListenerContainerFactory commonBankKafkaListenerContainerFactory,
            @Value("${spring.common-bank-kafka.topics.voe-survey.name}") String topicVoeSurvey,
            HazelcastService hazelcastService,
            ObjectMapper objectMapper) {
        this.hazelcastService = hazelcastService;
        this.objectMapper = objectMapper;
        log.info("topicVoeSurvey: {}", topicVoeSurvey);

        final MessageListenerContainer messageListenerContainer
                = commonBankKafkaListenerContainerFactory.createContainer(topicVoeSurvey);
        messageListenerContainer.setupMessageListener((MessageListener<String, String>) this::listen);
        messageListenerContainer.start();
    }

    private void listen(ConsumerRecord<String, String> data) {
        final Optional<VoeSurveyDto> voeSurveyDto = convertToVoeSurveyDto(data.value());
        if (
                voeSurveyDto.isPresent()
                        && ACTION_CREATE.equalsIgnoreCase(voeSurveyDto.get().getAction())
        ) {
            log.info("Receive VOE WITH create, need send to Hazelcast: {}", voeSurveyDto.get());
            try {
                hazelcastService.publishVoeSurvey(voeSurveyDto.get());
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        } else {
            log.info("Receive VOE WITHOUT create, IGNORE send to Hazelcast: {}", data.value());
        }
    }

    private Optional<VoeSurveyDto> convertToVoeSurveyDto(final String value) {
        try {
            return Optional.of(objectMapper.readValue(value, VoeSurveyDto.class));
        } catch (JsonProcessingException e) {
            log.error("convertToVoeSurveyDto(value={})", value);
            log.error(e.getMessage());
        }

        return Optional.empty();
    }
}
