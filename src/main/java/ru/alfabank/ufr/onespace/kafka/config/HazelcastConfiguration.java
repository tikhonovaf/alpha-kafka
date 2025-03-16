package ru.alfabank.ufr.onespace.kafka.config;

import static ru.alfabank.ufr.onespace.kafka.utils.HazelcastUtils.configureTopic;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.TopicOverloadPolicy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class HazelcastConfiguration {

    @Autowired
    private HazelcastConfig hazelcastConfig;

    @Autowired
    private HazelcastEntities hazelcastEntities;

    @Bean
    @ConditionalOnProperty(prefix = "interaction.hazelcast", name = "enabled", havingValue = "false", matchIfMissing = false)
    public HazelcastInstance getDevHazelcastInstance() {
        log.info("Starting local Hazelcast instance");

        Config cfg = new Config();
        NetworkConfig network = cfg.getNetworkConfig();

        JoinConfig join = network.getJoin();
        join.getTcpIpConfig().setEnabled(false);
        join.getMulticastConfig().setEnabled(false);

        new HashMap<String, Integer>() {{
            put("hazelcast.client.event.thread.count", 20);
            put("hazelcast.event.thread.count", 20);
            put("hazelcast.io.input.thread.count", 20);
            put("hazelcast.io.output.thread.count", 20);
            put("hazelcast.io.thread.count", 20);
        }}
            .entrySet()
            .forEach(prop -> {
                System.setProperty(prop.getKey(), String.valueOf(prop.getValue()));

                cfg.setProperty(
                    prop.getKey(),
                    String.valueOf(prop.getValue() > 5 ? prop.getValue() : 5)
                );
            });

        return configureHazelcastInstance(Hazelcast.newHazelcastInstance(cfg));
    }

    @Bean
    @ConditionalOnMissingBean(HazelcastInstance.class)
    public HazelcastInstance getProdHazelcastInstance() {
        log.info("Connecting to Hazelcast on {}", hazelcastConfig);
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setGroupConfig(new GroupConfig(hazelcastConfig.getLogin(), hazelcastConfig.getPassword()));
        clientConfig.getNetworkConfig()
            .setAddresses(hazelcastConfig.getHosts())
            .setConnectionAttemptLimit(hazelcastConfig.getConnectionAttemptLimit());

        return configureHazelcastInstance(HazelcastClient.newHazelcastClient(clientConfig));
    }

    private HazelcastInstance configureHazelcastInstance(final HazelcastInstance hazelcastInstance) {
        // Настройка топиков
        hazelcastEntities.getTopics().values().stream().forEach(topicConfig -> {
            log.info("configureHazelcastInstance. topic={}", topicConfig);
            configureTopic(hazelcastInstance, topicConfig, null);
        });

        return hazelcastInstance;
    }

    @Data
    @Configuration
    @ConfigurationProperties(prefix = "interaction.hazelcast")
    public static class HazelcastConfig {

        private String enabled;
        private String login;
        private String password;
        private Integer port;
        private List<String> hosts;
        private int connectionAttemptLimit;
    }

    @Data
    @Configuration
    @ConfigurationProperties(prefix = "interaction.hazelcast.entities")
    public static class HazelcastEntities {

        private Map<String, HazelcastMapConfig> maps;
        private Map<String, HazelcastTopicConfig> topics;

        @Data
        public static class HazelcastMapConfig {

            private String name;
            private Lifetime locktime;
            private Lifetime lifetime;
        }

        @Data
        public static class Lifetime {

            private TimeUnit unit;
            private Long value;
        }

        @Data
        public static class HazelcastTopicConfig {

            private String name;
            private boolean configRingbuffer;
            private int capacity = 10000;
            private TopicOverloadPolicy topicOverloadPolicy = TopicOverloadPolicy.BLOCK;
            private Lifetime lifetime;
        }
    }
}
