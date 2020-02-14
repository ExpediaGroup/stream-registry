/**
 * Copyright (C) 2018-2020 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.core.events;

import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.Optional;

import lombok.Data;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({KafkaNotificationListenerConfig.NewTopicProperties.class})
public class KafkaNotificationListenerConfig {
    public static final String KAFKA_BOOTSTRAP_SERVERS_PROPERTY = "spring.kafka.bootstrap-servers";
    public static final String KAFKA_NOTIFICATIONS_ENABLED_PROPERTY = "notification.events.kafka.enabled";
    public static final String KAFKA_TOPIC_NAME_PROPERTY = "notification.events.kafka.topic";
    public static final String KAFKA_TOPIC_SETUP_PROPERTY = "notification.events.kafka.topic.setup";

    @Value("${" + KAFKA_TOPIC_SETUP_PROPERTY + ":false}")
    private Boolean isKafkaSetupEnabled;

    @Value("${" + KAFKA_TOPIC_NAME_PROPERTY + ":#{null}}")
    private String notificationEventsTopic;

    @Value("${" + KAFKA_BOOTSTRAP_SERVERS_PROPERTY + ":#{null}}")
    private String bootstrapServers;

    @Bean(name = "kafkaNotificationEventListener")
    @ConditionalOnProperty(name = KAFKA_NOTIFICATIONS_ENABLED_PROPERTY)
    public KafkaNotificationEventListener kafkaNotificationEventListener(final NewTopicProperties newTopicProperties) {
        checkNotNull(bootstrapServers, getWarningMessageOnNotDefinedTopicProps("enabled notification events", KAFKA_BOOTSTRAP_SERVERS_PROPERTY));
        checkNotNull(notificationEventsTopic, getWarningMessageOnNotDefinedTopicProps("enabled notification events", KAFKA_TOPIC_NAME_PROPERTY));

        return createKafkaNotificationEventListener(newTopicProperties);
    }

    protected KafkaNotificationEventListener createKafkaNotificationEventListener(final NewTopicProperties newTopicProperties) {
        return KafkaNotificationEventListener.builder()
                .kafkaSetupHandler(createKafkaSetupHandlerIfEnabled(newTopicProperties))
                .build();
    }

    protected Optional<KafkaSetupHandler> createKafkaSetupHandlerIfEnabled(final NewTopicProperties newTopicProperties) {
        // This will set kafka setup only if 'notification.events.kafka.topic.setup' is true
        return Optional.ofNullable(isKafkaSetupEnabled)
                .filter(Boolean.TRUE::equals)
                .map(enabled -> KafkaSetupHandler.builder()
                        .newTopic(newTopicProperties.buildNewTopic(notificationEventsTopic))
                        .bootstrapServers(bootstrapServers)
                        .build());
    }

    @Data
    @ConfigurationProperties(prefix = KAFKA_TOPIC_SETUP_PROPERTY)
    public static class NewTopicProperties {
        private Integer numPartitions;
        private Short replicationFactor;
        private Map<String, String> configs = null;

        public NewTopic buildNewTopic(final String topicName) {
            // We execute this method and its validations only when 'notification.events.kafka.topic.setup' is true
            // during KafkaSetupHandler building. If we use @Validated and its constraints, bean loading will fail even
            // when topic setup is disabled.
            final String component = "enabled Kafka topic setup";

            checkNotNull(topicName, getWarningMessageOnNotDefinedTopicProps(component, KAFKA_TOPIC_NAME_PROPERTY));
            checkNotNull(numPartitions, getWarningMessageOnNotDefinedTopicProps(component,
                    KAFKA_TOPIC_SETUP_PROPERTY.concat(".numPartitions")));
            checkNotNull(replicationFactor, getWarningMessageOnNotDefinedTopicProps(component,
                    KAFKA_TOPIC_SETUP_PROPERTY.concat(".replicationFactor")));

            final String gtZeroWarning = " must be greater than zero";

            checkArgument(numPartitions.compareTo(0) > 0,
                    KAFKA_TOPIC_SETUP_PROPERTY.concat(".numPartitions").concat(gtZeroWarning));
            checkArgument(replicationFactor.intValue() > 0,
                    KAFKA_TOPIC_SETUP_PROPERTY.concat(".replicationFactor").concat(gtZeroWarning));

            return new NewTopic(topicName, numPartitions, replicationFactor).configs(configs);
        }
    }

    public static String getWarningMessageOnNotDefinedTopicProps(String component, String property) {
        return String.format("%s prop must be configured on %s", property, component);
    }
}