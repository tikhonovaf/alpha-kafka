package ru.alfabank.ufr.onespace.kafka.service;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class HazelcastLogListener implements MessageListener<String> {

    private final HazelcastService hazelcastService;
    private volatile String listenerId;

    @PostConstruct
    private void init() {
        reinit();
    }

    public synchronized String reinit() {
        final String newListenerId = hazelcastService.getVoeSurverTopic().addMessageListener(this);
        log.info("reinit(newListenerId={})", newListenerId);

        // Отключаем листенер
        remove();

        this.listenerId = newListenerId;
        return this.listenerId;
    }

    public void remove() {
        if (isNotEmpty(listenerId)) {
            hazelcastService.getVoeSurverTopic().removeMessageListener(listenerId);
        }
        this.listenerId = null;
    }

    @Override
    public void onMessage(Message<String> message) {
        log.info("HazelcastLogListener.onMessage({})", message.getMessageObject());
    }
}
