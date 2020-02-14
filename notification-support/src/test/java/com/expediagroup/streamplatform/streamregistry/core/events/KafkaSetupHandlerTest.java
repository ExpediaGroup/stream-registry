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
import static org.junit.Assert.*;

import lombok.extern.slf4j.Slf4j;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = MockListenerConfiguration.class,
        properties = {
                KAFKA_NOTIFICATIONS_ENABLED_PROPERTY + "=true",
                KAFKA_TOPIC_SETUP_PROPERTY + "=" + KafkaSetupHandlerTest.TEST_TOPIC_SETUP_ENABLED,
                KAFKA_BOOTSTRAP_SERVERS_PROPERTY + "=" + KafkaSetupHandlerTest.TEST_BOOTSTRAP_SERVERS,
                KAFKA_TOPIC_NAME_PROPERTY + "=" + KafkaSetupHandlerTest.TEST_TOPIC,
                KAFKA_TOPIC_SETUP_PROPERTY + ".numPartitions=" + KafkaSetupHandlerTest.TEST_PARTITIONS,
                KAFKA_TOPIC_SETUP_PROPERTY + ".replicationFactor=" + KafkaSetupHandlerTest.TEST_REPLICATION_FACTOR
        }
)
public class KafkaSetupHandlerTest {
    public static final String TEST_TOPIC_SETUP_ENABLED = "true";
    public static final String TEST_BOOTSTRAP_SERVERS = "a:9092,b:9092";
    public static final String TEST_TOPIC = "my-topic";
    public static final String TEST_PARTITIONS = "2";
    public static final String TEST_REPLICATION_FACTOR = "1";

    @Autowired
    private KafkaNotificationEventListener kafkaNotificationEventListener;

    @Value("${" + KAFKA_BOOTSTRAP_SERVERS_PROPERTY + "}")
    private String bootstrapServers;

    @Value("${" + KAFKA_TOPIC_NAME_PROPERTY + "}")
    private String notificationEventsTopic;

    @Value("${" + KAFKA_TOPIC_SETUP_PROPERTY + ".numPartitions" + "}")
    private Integer numPartitions;

    @Value("${" + KAFKA_TOPIC_SETUP_PROPERTY + ".replicationFactor" + "}")
    private Integer replicationFactor;

    @Value("${" + KAFKA_TOPIC_SETUP_PROPERTY + ":false}")
    private boolean isKafkaSetupEnabled;

    @Test
    public void having_config_set_verify_they_are_being_loaded_and_match_with_name_and_value() {
        assertEquals("Bootstrap servers don't match", bootstrapServers, TEST_BOOTSTRAP_SERVERS);
        assertEquals("Notification topic doesn't match", notificationEventsTopic, TEST_TOPIC);
        assertEquals("Num. partitions don't match", numPartitions.intValue(), Integer.parseInt(TEST_PARTITIONS));
        assertEquals("Replication factor doesn't match", replicationFactor.intValue(), Integer.parseInt(TEST_REPLICATION_FACTOR));
        assertEquals("Kafka setup flag doesn't match", isKafkaSetupEnabled, Boolean.parseBoolean(TEST_TOPIC_SETUP_ENABLED));
    }

    @Test
    public void having_topic_setup_enabled_verify_that_KafkaSetupHandler_setup_method_is_being_called_in_postconstruct() {
        assertTrue("KafkaSetupHandler should be present", kafkaNotificationEventListener.getKafkaSetupHandler().isPresent());

        // This is our spy from MockListenerConfiguration!
        KafkaSetupHandler handler = kafkaNotificationEventListener.getKafkaSetupHandler().get();

        // setup() is being called from kafkaNotificationEventListener.init() method which is annotated with @PostConstruct
        Mockito.verify(handler, Mockito.timeout(5000).times(1)).setup();
    }
}
