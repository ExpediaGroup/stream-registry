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

import java.util.Objects;
import java.util.Optional;

import org.apache.avro.specific.SpecificRecord;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.expediagroup.streamplatform.streamregistry.core.events.handlers.ProducerEventHandlerForKafka;
import com.expediagroup.streamplatform.streamregistry.core.events.handlers.SchemaEventHandlerForKafka;
import com.expediagroup.streamplatform.streamregistry.core.events.handlers.StreamBindingEventHandlerForKafka;
import com.expediagroup.streamplatform.streamregistry.core.events.handlers.StreamEventHandlerForKafka;

@RunWith(SpringRunner.class)// Explicitly defined prop with true as value
@SpringBootTest(classes = NotificationEventListenerSuccessfulLoadingTest.MockListenerConfiguration.class,
    properties = {
        KAFKA_NOTIFICATIONS_ENABLED_PROPERTY + "=true",
        KAFKA_TOPIC_NAME_PROPERTY + "=my-topic",
        KAFKA_TOPIC_SETUP_PROPERTY + "=false", // We don't test setup topic here but in  the integration test
        KAFKA_BOOTSTRAP_SERVERS_PROPERTY + "=localhost:9092",
        KAFKA_SCHEMA_REGISTRY_URL_PROPERTY + "=foo:8081"})
public class NotificationEventListenerSuccessfulLoadingTest {

  @Autowired(required = false)
  private Optional<SchemaEventHandlerForKafka> schemaEventHandlerForKafka;

  @Autowired(required = false)
  private Optional<StreamEventHandlerForKafka> streamEventHandlerForKafka;

  @Autowired(required = false)
  private Optional<StreamBindingEventHandlerForKafka> streamBindingEventHandlerForKafka;

  @Autowired(required = false)
  private Optional<ProducerEventHandlerForKafka> producerEventHandlerForKafka;

  @Test
  public void having_notifications_enabled_verify_that_KafkaNotificationEventListener_is_being_loaded() {
    Assert.assertNotNull("Optional container of SchemaEventHandlerForKafka shouldn't be null!", schemaEventHandlerForKafka);
    Assert.assertTrue(String.format("Kafka schema event handler should be loaded since %s == true", KAFKA_NOTIFICATIONS_ENABLED_PROPERTY), schemaEventHandlerForKafka.isPresent());

    Assert.assertNotNull("Optional container of StreamEventHandlerForKafka shouldn't be null!", streamEventHandlerForKafka);
    Assert.assertTrue(String.format("Kafka stream event handler should be loaded since %s == true", KAFKA_NOTIFICATIONS_ENABLED_PROPERTY), streamEventHandlerForKafka.isPresent());

    Assert.assertNotNull("Optional container of StreamBindingEventHandlerForKafka shouldn't be null!", streamBindingEventHandlerForKafka);
    Assert.assertTrue(String.format("Kafka streamBinding event handler should be loaded since %s == true", KAFKA_NOTIFICATIONS_ENABLED_PROPERTY), streamBindingEventHandlerForKafka.isPresent());

    Assert.assertNotNull("Optional container of ProducerEventHandlerForKafka shouldn't be null!", producerEventHandlerForKafka);
    Assert.assertTrue(String.format("Kafka producer event handler should be loaded since %s == true", KAFKA_NOTIFICATIONS_ENABLED_PROPERTY), producerEventHandlerForKafka.isPresent());
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