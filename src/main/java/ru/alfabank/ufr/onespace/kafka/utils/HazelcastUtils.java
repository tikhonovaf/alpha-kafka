package ru.alfabank.ufr.onespace.kafka.utils;

import static net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.hazelcast.config.ConfigurationException;
import com.hazelcast.config.ReliableTopicConfig;
import com.hazelcast.config.RingbufferConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;
import lombok.extern.slf4j.Slf4j;
import ru.alfabank.ufr.onespace.kafka.config.HazelcastConfiguration;

@Slf4j
public class HazelcastUtils {

    public static final String DELIMITER = "-";

    public static void configureTopic(
        final HazelcastInstance hazelcastInstance,
        final HazelcastConfiguration.HazelcastEntities.HazelcastTopicConfig topicConfig,
        final String suffix
    ) {
        if (topicConfig.isConfigRingbuffer()) {
            try {
                final String topicName = getTopicName(topicConfig.getName(), suffix);
                log.info("Config topicName: {} with config: {}", topicName, topicConfig);

                // Настройка RingBuffer, который используется в топике
                final RingbufferConfig ringbufferConfig = new RingbufferConfig(topicName);
                ringbufferConfig.setCapacity(topicConfig.getCapacity());
                ringbufferConfig.setTimeToLiveSeconds(
                    (int) topicConfig.getLifetime().getUnit().toSeconds(topicConfig.getLifetime().getValue())
                );
                hazelcastInstance
                    .getConfig()
                    .addRingBufferConfig(ringbufferConfig);

                final ReliableTopicConfig reliableTopicConfig = new ReliableTopicConfig(topicName);
                reliableTopicConfig.setTopicOverloadPolicy(topicConfig.getTopicOverloadPolicy());
                hazelcastInstance.getConfig().addReliableTopicConfig(reliableTopicConfig);
            } catch (ConfigurationException e) {
                log.error("Config topic: {} with config error: {}", topicConfig, e.getMessage());
            } catch (Exception e) {
                log.error("Config topic: {} with other error: {}", topicConfig, e.getMessage());
            }
        }
    }

    public static String getTopicName(
        final String topicName,
        final String suffix
    ) {
        if (isEmpty(suffix)) {
            return topicName;
        }

        return new StringBuffer()
            .append(topicName)
            .append(DELIMITER)
            .append(suffix)
            .toString();
    }

    public static String addMessageListener(
        final ITopic<String> topic,
        final String oldListenerId,
        final MessageListener<String> listener
    ) {
        final String newListenerId = topic.addMessageListener(listener);

        if (isNotEmpty(oldListenerId)) {
            topic.removeMessageListener(oldListenerId);
        }

        return newListenerId;
    }
}
