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

import java.util.Optional;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.admin.NewTopic;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Slf4j
@Configuration
public class MockListenerConfiguration extends KafkaNotificationListenerConfig {

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

    protected KafkaNotificationEventListener createKafkaNotificationEventListener(NewTopicProperties newTopicProperties, SchemaParserProperties parserProperties) {
        return Mockito.spy(super.createKafkaNotificationEventListener(newTopicProperties, parserProperties));
    }

    protected Optional<KafkaSetupHandler> createKafkaSetupHandlerIfEnabled(final NewTopicProperties newTopicProperties) {
        log.info("Building mock KafkaSetupHandler with superclass constraints...");

        // It is going to return a mock instead the real one!
        return super.createKafkaSetupHandlerIfEnabled(newTopicProperties).map(real -> {
            KafkaSetupHandler handler = Mockito.spy(new KafkaSetupHandlerMock(real.getBootstrapServers(), real.getNewTopic()));
            return handler;
        });
    }

    public static class KafkaSetupHandlerMock extends KafkaSetupHandler {
        public KafkaSetupHandlerMock(@NonNull String bootstrapServers, @NonNull NewTopic newTopic) {
            super(bootstrapServers, newTopic);
        }

        public void setup() {
            log.info("Do test set up :)");
        }
    }
}