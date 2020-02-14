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

import static com.expediagroup.streamplatform.streamregistry.core.events.KafkaNotificationListenerConfig.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@EmbeddedKafka
@SpringBootTest(
        classes = KafkaNotificationEventListenerIntegrationTest.SpyListenerConfiguration.class,
        properties = {
                KAFKA_NOTIFICATIONS_ENABLED_PROPERTY + "=true",
                KAFKA_TOPIC_NAME_PROPERTY + "=" + KafkaNotificationEventListenerIntegrationTest.TEST_NOTIFICATION_TOPIC,
                KAFKA_TOPIC_SETUP_PROPERTY + "=true",
                KAFKA_TOPIC_SETUP_PROPERTY + ".numPartitions=" + KafkaNotificationEventListenerIntegrationTest.TEST_PARTITIONS,
                KAFKA_TOPIC_SETUP_PROPERTY + ".replicationFactor=" + KafkaNotificationEventListenerIntegrationTest.TEST_REPLICATION_FACTOR,
                KAFKA_BOOTSTRAP_SERVERS_PROPERTY + "=${spring.embedded.kafka.brokers}",
                KAFKA_SCHEMA_REGISTRY_URL_PROPERTY + "=foo:8081"
        })
public class KafkaNotificationEventListenerIntegrationTest {
    public static final String TEST_NOTIFICATION_TOPIC = "test-notification-topic";
    public static final String TEST_PARTITIONS = "2";
    public static final String TEST_REPLICATION_FACTOR = "1";

    @Value("${" + KAFKA_BOOTSTRAP_SERVERS_PROPERTY + "}")
    private String bootstrapServers;

    @Autowired
    private KafkaNotificationEventListener kafkaNotificationEventListener;

    @Test
    public void having_topic_setup_enabled_verify_that_notification_topic_is_being_created() throws ExecutionException, InterruptedException {
        Optional<KafkaSetupHandler> handler = kafkaNotificationEventListener.getKafkaSetupHandler();

        Assert.assertTrue(String.format("Handler must be configured if %s is true", KAFKA_TOPIC_SETUP_PROPERTY), handler.isPresent());

        Mockito.verify(handler.get(), Mockito.timeout(5000).times(1)).setup();

        try (AdminClient client = createAdminClient()) {
            Optional<TopicDescription> desc = obtainTopicDescription(client, TEST_NOTIFICATION_TOPIC);
            Assert.assertTrue(String.format("Topic %s should be present", TEST_NOTIFICATION_TOPIC), desc.isPresent());
        }
    }

    @Slf4j
    @Configuration
    public static class SpyListenerConfiguration extends KafkaNotificationListenerConfig {
        protected KafkaNotificationEventListener createKafkaNotificationEventListener(final NewTopicProperties newTopicProperties) {
            return Optional.of(super.createKafkaNotificationEventListener(newTopicProperties)).map(Mockito::spy).get();
        }

        protected Optional<KafkaSetupHandler> createKafkaSetupHandlerIfEnabled(final NewTopicProperties newTopicProperties) {
            return super.createKafkaSetupHandlerIfEnabled(newTopicProperties).map(Mockito::spy);
        }
    }

    private Optional<TopicDescription> obtainTopicDescription(AdminClient client, String topic) throws ExecutionException, InterruptedException {
        try {
            log.info("Verifying existence of topic {}", topic);

            return Optional.ofNullable(client.describeTopics(Collections.singleton(topic)).all().get().get(topic));
        } catch (ExecutionException exception) {
            if (exception.getCause() != null && exception.getCause() instanceof UnknownTopicOrPartitionException) {
                return Optional.empty();
            } else throw exception;
        }
    }

    private AdminClient createAdminClient() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        log.info("Creating a Kafka Admin client with configuration {}", configs);

        return AdminClient.create(configs);
    }
}