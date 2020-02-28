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

import static com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventConfig.KAFKA_NOTIFICATIONS_ENABLED_PROPERTY;

import java.util.Objects;
import java.util.Optional;

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
import org.springframework.test.context.junit4.SpringRunner;

import com.expediagroup.streamplatform.streamregistry.core.events.handlers.SchemaEventHandlerForKafka;

@RunWith(SpringRunner.class)// Explicitly defined prop with false as value
@SpringBootTest(
    classes = EventHandlerAvoidLoadingTest.MockListenerConfiguration.class,
    properties = {
        KAFKA_NOTIFICATIONS_ENABLED_PROPERTY + "=false"
    })
public class EventHandlerAvoidLoadingTest {

  @Autowired(required = false)
  private Optional<SchemaEventHandlerForKafka> schemaEventHandlerForKafka;

  @Test
  public void having_notifications_disabled_verify_that_KafkaNotificationEventListener_is_not_being_loaded() {
    Assert.assertNotNull("Optional container of SchemaEventHandlerForKafka shouldn't be null!", schemaEventHandlerForKafka);
    Assert.assertFalse("Kafka notification should NOT be loaded since notification.events.kafka.enabled == false", schemaEventHandlerForKafka.isPresent());
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
  }
}