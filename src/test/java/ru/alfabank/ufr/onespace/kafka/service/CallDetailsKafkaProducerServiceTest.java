package ru.alfabank.ufr.onespace.kafka.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
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
import ru.alfabank.ufr.onespace.kafka.model.CallDetailsDto;

import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1,
        topics = {"${spring.kafka.topic.name}"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class CallDetailsKafkaProducerServiceTest {

    @Autowired
    private CallDetailsKafkaProducerService callDetailsKafkaProducerService;

    @SpyBean(name = "businessResultKafkaTemplate")
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.name}")
    private String topicName;

    @Test
    public void testSendMessageRetries() {

        AtomicInteger callCount = new AtomicInteger();

        doAnswer(new Answer<>() {
            public Object answer(InvocationOnMock invocation) {
                if (callCount.incrementAndGet() <= 4) {
                    throw new RuntimeException("Test exception");
                }

                SettableListenableFuture<SendResult<String, Object>> future = new SettableListenableFuture<>();
                future.set(new SendResult<>(null, null));
                return future;
            }
        }).when(kafkaTemplate).send(any(String.class), any(Object.class));

        CallDetailsDto message = new CallDetailsDto();
        message.setPomSessionId("12345");

        callDetailsKafkaProducerService.sendMessage(message);

        Mockito.verify(kafkaTemplate, times(5)).send(topicName, message);
    }
}
