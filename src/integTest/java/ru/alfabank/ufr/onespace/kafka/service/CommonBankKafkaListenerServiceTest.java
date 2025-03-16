package ru.alfabank.ufr.onespace.kafka.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static ru.alfabank.ufr.onespace.kafka.model.VoeSurveyDto.ACTION_CREATE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import ru.alfabank.ufr.onespace.kafka.model.VoeSurveyDto;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@EmbeddedKafka(partitions = 1,
    topics = {"${spring.common-bank-kafka.topics.voe-survey.name}"},
    brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class CommonBankKafkaListenerServiceTest {

    @Qualifier("commonBankKafkaTemplate")
    @Autowired
    private KafkaTemplate<String, Object> commonBankKafkaTemplate;

    @Value("${spring.common-bank-kafka.topics.voe-survey.name}")
    private String topicVoeSurvey;

    @Autowired
    private HazelcastService hazelcastService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void receiveVoeSurveySuccess() throws InterruptedException, JsonProcessingException {
        final List<VoeSurveyDto> receivedVoeSurveys = new ArrayList<>();

        hazelcastService.getVoeSurverTopic().addMessageListener(new MessageListener<String>() {
            @Override
            public void onMessage(Message<String> message) {
                try {
                    receivedVoeSurveys.add(objectMapper.readValue(message.getMessageObject(), VoeSurveyDto.class));
                } catch (JsonProcessingException e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        final VoeSurveyDto voeSurveyDtoIgnore = VoeSurveyDto
            .builder()
            .userLogin("login-1")
            .surveyId("survey-id-1")
            .action("update")
            .build();

        final VoeSurveyDto voeSurveyDtoSuccess = VoeSurveyDto
            .builder()
            .userLogin("login-2")
            .surveyId("survey-id-2")
            .action(ACTION_CREATE.toLowerCase())
            .build();

        final VoeSurveyDto voeSurveyDtoSuccess2 = VoeSurveyDto
            .builder()
            .userLogin("login-3")
            .surveyId("survey-id-3")
            .action(ACTION_CREATE.toUpperCase())
            .build();

        commonBankKafkaTemplate.send(topicVoeSurvey, "wrong text");
        commonBankKafkaTemplate.send(topicVoeSurvey, "{\"test\": \"test\"}");
        commonBankKafkaTemplate.send(topicVoeSurvey, objectMapper.writeValueAsString(voeSurveyDtoIgnore));
        commonBankKafkaTemplate.send(topicVoeSurvey, objectMapper.writeValueAsString(voeSurveyDtoSuccess));
        commonBankKafkaTemplate.send(topicVoeSurvey, objectMapper.writeValueAsString(voeSurveyDtoSuccess2));

        TimeUnit.SECONDS.sleep(2);

        log.info("receivedVoeSurveys: {}", receivedVoeSurveys);

        // THEN
        assertNotNull(voeSurveyDtoSuccess);
        assertEquals(2, receivedVoeSurveys.size());
        assertEquals(voeSurveyDtoSuccess.getSurveyId(), receivedVoeSurveys.get(0).getSurveyId());
        assertEquals(voeSurveyDtoSuccess.getUserLogin(), receivedVoeSurveys.get(0).getUserLogin());
    }

}
