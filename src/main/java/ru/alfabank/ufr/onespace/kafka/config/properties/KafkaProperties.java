package ru.alfabank.ufr.onespace.kafka.config.properties;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Consumer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Producer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Security;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Ssl;
import org.springframework.util.CollectionUtils;

@Data
@Slf4j
public class KafkaProperties {

    private String bootstrapServers;
    private Map<String, String> properties = new HashMap<>();
    private Producer producer;
    private Consumer consumer;
    private Ssl ssl = new Ssl();
    private Security security = new Security();
    private boolean allowMockRest;

    public Map<String, Object> getConsumerProperties() {
        final Map<String, Object> props = new HashMap<>();

        props.putAll(getCommonProperties());

        if (this.getConsumer() != null) {
            props.putAll(this.getConsumer().buildProperties());
        }

        return props;
    }

    public Map<String, Object> getProducerProperties() {
        final Map<String, Object> props = new HashMap<>();

        props.putAll(getCommonProperties());

        if (this.getProducer() != null) {
            props.putAll(this.getProducer().buildProperties());
        }

        return props;
    }

    private Map<String, Object> getCommonProperties() {
        final Map<String, Object> props = new HashMap<>();
        if (isNotEmpty(this.getBootstrapServers())) {
            props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, this.getBootstrapServers());
        }
        props.putAll(this.getSsl().buildProperties());
        props.putAll(this.getSecurity().buildProperties());
        if (!CollectionUtils.isEmpty(this.getProperties())) {
            props.putAll(this.getProperties());
        }
        return props;
    }

}
