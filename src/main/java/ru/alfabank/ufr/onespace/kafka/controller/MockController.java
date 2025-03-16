package ru.alfabank.ufr.onespace.kafka.controller;


import static ru.alfabank.ufr.onespace.kafka.model.VoeSurveyDto.ACTION_CREATE;

import javax.validation.Valid;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.alfabank.ufr.onespace.kafka.model.VoeSurveyDto;
import ru.alfabank.ufr.onespace.kafka.service.HazelcastService;

@RestController
@RequestMapping(
    value = "/mock",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    prefix = "spring.common-bank-kafka", name = "allow-mock-rest", havingValue = "true", matchIfMissing = false
)
public class MockController {

    @Qualifier("commonBankKafkaTemplate")
    private final KafkaTemplate<String, Object> commonBankKafkaTemplate;

    private final HazelcastService hazelcastService;
    private final ObjectMapper objectMapper;

    @Value("${spring.common-bank-kafka.topics.voe-survey.name}")
    private String topicVoeSurvey;

    @SneakyThrows
    @PostMapping("/to-kafka")
    public VoeSurveyDto sendVoeSurveyToKafka(
        @Valid @RequestBody VoeSurveyDto voeSurveyDto
    ) {
        log.info("Send to kafka: {}", voeSurveyDto);
         commonBankKafkaTemplate.send(topicVoeSurvey, objectMapper.writeValueAsString(voeSurveyDto));

        return voeSurveyDto;
    }

    @PostMapping("/to-hazelcast")
    public VoeSurveyDto sendVoeSurveyToHazelcast(
        @Valid @RequestBody VoeSurveyDto voeSurveyDto
    ) {
        log.info("Send to hazelcast: {}", voeSurveyDto);
        if (ACTION_CREATE.equalsIgnoreCase(voeSurveyDto.getAction())) {
            hazelcastService.publishVoeSurvey(voeSurveyDto);

            return voeSurveyDto;
        }

        throw new RuntimeException("Отправка для action=" + voeSurveyDto.getAction() + " не предусмотрено");
    }
}
