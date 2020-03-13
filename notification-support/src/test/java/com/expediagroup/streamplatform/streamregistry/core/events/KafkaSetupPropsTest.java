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

import static com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventUtils.getWarningMessageOnNotDefinedProp;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.KAFKA_BOOTSTRAP_SERVERS_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.KAFKA_NOTIFICATIONS_ENABLED_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.KAFKA_SCHEMA_REGISTRY_URL_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.KAFKA_TOPIC_NAME_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.KAFKA_TOPIC_SETUP_PROPERTY;
import static org.junit.Assert.assertEquals;

import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.apache.avro.specific.SpecificRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.junit4.SpringRunner;

import com.expediagroup.streamplatform.streamregistry.core.events.config.NewTopicProperties;
import com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = KafkaSetupPropsTest.MockListenerConfiguration.class,
    properties = {
        KAFKA_NOTIFICATIONS_ENABLED_PROPERTY + "=true",
        KAFKA_TOPIC_SETUP_PROPERTY + "=" + KafkaSetupPropsTest.TEST_TOPIC_SETUP_ENABLED,
        KAFKA_BOOTSTRAP_SERVERS_PROPERTY + "=" + KafkaSetupPropsTest.TEST_BOOTSTRAP_SERVERS,
        KAFKA_TOPIC_NAME_PROPERTY + "=" + KafkaSetupPropsTest.TEST_TOPIC,
        KAFKA_TOPIC_SETUP_PROPERTY + ".numPartitions=" + KafkaSetupPropsTest.TEST_PARTITIONS,
        KAFKA_TOPIC_SETUP_PROPERTY + ".replicationFactor=" + KafkaSetupPropsTest.TEST_REPLICATION_FACTOR,
        KAFKA_SCHEMA_REGISTRY_URL_PROPERTY + "=foo:8081"
    }
)
public class KafkaSetupPropsTest {
  public static final String TEST_TOPIC_SETUP_ENABLED = "true";
  public static final String TEST_BOOTSTRAP_SERVERS = "a:9092,b:9092";
  public static final String TEST_TOPIC = "my-topic";
  public static final String TEST_PARTITIONS = "2";
  public static final String TEST_REPLICATION_FACTOR = "1";

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

  @Configuration
  public static class MockListenerConfiguration extends NotificationEventConfig {
    @Value("${" + KAFKA_TOPIC_NAME_PROPERTY + ":#{null}}")
    private String notificationEventsTopic;

    @Value("${" + KAFKA_BOOTSTRAP_SERVERS_PROPERTY + ":#{null}}")
    private String bootstrapServers;

    @Bean(initMethod = "setup")
    @ConditionalOnProperty(name = KAFKA_NOTIFICATIONS_ENABLED_PROPERTY)
    public KafkaSetupHandler kafkaSetupHandler(NewTopicProperties newTopicProperties) {
      Objects.requireNonNull(notificationEventsTopic, getWarningMessageOnNotDefinedProp("enabled notification events", KAFKA_TOPIC_NAME_PROPERTY));
      Objects.requireNonNull(bootstrapServers, getWarningMessageOnNotDefinedProp("enabled notification events", KAFKA_BOOTSTRAP_SERVERS_PROPERTY));

      return Mockito.mock(KafkaSetupHandler.class);
    }

    @Bean(name = "producerFactory")
    @ConditionalOnProperty(name = KAFKA_NOTIFICATIONS_ENABLED_PROPERTY)
    public ProducerFactory<SpecificRecord, SpecificRecord> producerFactory() {
      return Mockito.mock(ProducerFactory.class);
    }

    @Bean(name = "kafkaTemplate")
    @ConditionalOnProperty(name = KAFKA_NOTIFICATIONS_ENABLED_PROPERTY)
    public KafkaTemplate<SpecificRecord, SpecificRecord> kafkaTemplate() {
      return Mockito.mock(KafkaTemplate.class);
    }
  }
}