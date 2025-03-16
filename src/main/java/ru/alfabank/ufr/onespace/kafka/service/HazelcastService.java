package ru.alfabank.ufr.onespace.kafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alfabank.ufr.onespace.kafka.config.HazelcastConfiguration;
import ru.alfabank.ufr.onespace.kafka.model.VoeSurveyDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class HazelcastService {

    private static final String TOPIC_VOE_SURVEY = "voe-survey";

    private final HazelcastInstance hzInstance;
    private final HazelcastConfiguration.HazelcastEntities hazelcastEntities;
    private final ObjectMapper objectMapper;

    /**
     * Отправляем VOE survey
     */
    @SneakyThrows
    public void publishVoeSurvey(VoeSurveyDto voeSurveyDto) {
        log.info("publishVoeSurvey(voeSurveyDto={})", voeSurveyDto);
        getVoeSurverTopic().publish(objectMapper.writeValueAsString(voeSurveyDto));
    }

    public ITopic<String> getVoeSurverTopic() {
        return hzInstance.getReliableTopic(
            hazelcastEntities.getTopics().get(TOPIC_VOE_SURVEY).getName());
    }
}
