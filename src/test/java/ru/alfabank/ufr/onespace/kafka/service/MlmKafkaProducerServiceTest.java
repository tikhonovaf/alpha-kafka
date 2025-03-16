package ru.alfabank.ufr.onespace.kafka.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.concurrent.SettableListenableFuture;
import ru.alfabank.ufr.onespace.kafka.model.MlmDto;

import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1,
        topics = {"${spring.mlm-kafka.topic.name}"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class MlmKafkaProducerServiceTest {

    @Autowired
    private MlmInfoKafkaProducerService service;

    @SpyBean(name = "mlmKafkaTemplate")
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.mlm-kafka.topic.name}")
    private String topicName;

    @Test
    void testSendMessageRetries() {
        AtomicInteger callCount = new AtomicInteger();

        doAnswer((Answer<Object>) invocation -> {
            if (callCount.incrementAndGet() <= 4) {
                throw new RuntimeException("Test exception");
            }

            SettableListenableFuture<SendResult<String, Object>> future = new SettableListenableFuture<>();
            future.set(new SendResult<>(null, null));
            return future;
        }).when(kafkaTemplate).send(any(String.class), any(String.class), any(Object.class));

        MlmDto message = new MlmDto(null, "2312312", null, null, null, "U_pin");

        service.sendMessage(message);

        Mockito.verify(kafkaTemplate, times(5)).send(topicName, message.operatorId(), message);
    }
}
