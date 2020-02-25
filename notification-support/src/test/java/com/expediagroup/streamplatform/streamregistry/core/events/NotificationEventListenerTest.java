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

import static com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventConfig.*;

import java.util.Objects;
import java.util.stream.IntStream;

import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;

import com.expediagroup.streamplatform.streamregistry.core.events.handlers.SchemaEventHandlerForKafka;
import com.expediagroup.streamplatform.streamregistry.core.events.listeners.SchemaNotificationEventListener;
import com.expediagroup.streamplatform.streamregistry.core.events.listeners.StreamNotificationEventListener;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

@Slf4j
@RunWith(SpringRunner.class)// Explicitly defined prop with true as value
@SpringBootTest(classes = {NotificationEventListenerTest.MockListenerConfiguration.class},
        properties = {
                KAFKA_NOTIFICATIONS_ENABLED_PROPERTY + "=true",
                KAFKA_TOPIC_NAME_PROPERTY + "=my-topic",
                KAFKA_TOPIC_SETUP_PROPERTY + "=false", // We don't test setup topic here but in  the integration test
                KAFKA_BOOTSTRAP_SERVERS_PROPERTY + "=localhost:9092",
                KAFKA_SCHEMA_REGISTRY_URL_PROPERTY + "=foo:8081"})
public class NotificationEventListenerTest {
    public static final int TEST_CREATE_SCHEMA_EVENTS = 5;
    public static final int TEST_UPDATE_SCHEMA_EVENTS = 3;
    public static final int TEST_DELETE_SCHEMA_EVENTS = 2;

    public static final int TEST_CREATE_STREAM_EVENTS = 1;
    public static final int TEST_UPDATE_STREAM_EVENTS = 4;
    public static final int TEST_DELETE_STREAM_EVENTS = 6;

    @Autowired
    private ApplicationEventMulticaster applicationEventMulticaster;

    @SpyBean
    private StreamNotificationEventListener streamNotificationEventListener;

    @Autowired
    private SchemaNotificationEventListener schemaNotificationEventListener;

    @Autowired
    private SchemaEventHandlerForKafka schemaEventHandlerForKafka;

    @Before
    public void before() {
        IntStream.rangeClosed(1, TEST_CREATE_SCHEMA_EVENTS).forEachOrdered(i -> applicationEventMulticaster.multicastEvent(getDummySchemaEvent(i, EventType.CREATE, "schema-create")));
        IntStream.rangeClosed(1, TEST_UPDATE_SCHEMA_EVENTS).forEachOrdered(i -> applicationEventMulticaster.multicastEvent(getDummySchemaEvent(i, EventType.UPDATE, "schema-update")));
        IntStream.rangeClosed(1, TEST_DELETE_SCHEMA_EVENTS).forEachOrdered(i -> applicationEventMulticaster.multicastEvent(getDummySchemaEvent(i, EventType.DELETE, "schema-delete")));

        IntStream.rangeClosed(1, TEST_CREATE_STREAM_EVENTS).forEachOrdered(i -> applicationEventMulticaster.multicastEvent(getDummyStreamEvent(i, EventType.CREATE, "stream-create")));
        IntStream.rangeClosed(1, TEST_UPDATE_STREAM_EVENTS).forEachOrdered(i -> applicationEventMulticaster.multicastEvent(getDummyStreamEvent(i, EventType.UPDATE, "stream-update")));
        IntStream.rangeClosed(1, TEST_DELETE_STREAM_EVENTS).forEachOrdered(i -> applicationEventMulticaster.multicastEvent(getDummyStreamEvent(i, EventType.DELETE, "stream-delete")));
    }

    @Test
    public void having_pushed_events_verify_that_listener_methods_are_executed() {
        log.info("Bean {} has the event listener and contains a list of handlers, in this case only one {}", schemaNotificationEventListener, schemaEventHandlerForKafka);

        // Here we check that the listener call the list of handlers which contains our mock handler
        Mockito.verify(schemaEventHandlerForKafka, Mockito.timeout(5000).times(TEST_CREATE_SCHEMA_EVENTS)).onCreate(Mockito.notNull());
        Mockito.verify(schemaEventHandlerForKafka, Mockito.timeout(5000).times(TEST_UPDATE_SCHEMA_EVENTS)).onUpdate(Mockito.notNull());
        Mockito.verify(schemaEventHandlerForKafka, Mockito.timeout(5000).times(TEST_DELETE_SCHEMA_EVENTS)).onDelete(Mockito.notNull());

        // Here we check that the listener gets called
        Mockito.verify(streamNotificationEventListener, Mockito.timeout(5000).times(TEST_CREATE_STREAM_EVENTS)).onCreate(Mockito.notNull());
        Mockito.verify(streamNotificationEventListener, Mockito.timeout(5000).times(TEST_UPDATE_STREAM_EVENTS)).onUpdate(Mockito.notNull());
        Mockito.verify(streamNotificationEventListener, Mockito.timeout(5000).times(TEST_DELETE_STREAM_EVENTS)).onDelete(Mockito.notNull());
    }

    @Configuration
    @ComponentScan(basePackageClasses = {SchemaNotificationEventListener.class, StreamNotificationEventListener.class})
    public static class MockListenerConfiguration extends NotificationEventConfig {
        @Value("${" + KAFKA_TOPIC_NAME_PROPERTY + ":#{null}}")
        private String notificationEventsTopic;

        @Value("${" + KAFKA_BOOTSTRAP_SERVERS_PROPERTY + ":#{null}}")
        private String bootstrapServers;

        @Bean
        public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
            SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
            eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());

            return eventMulticaster;
        }

        @Bean(initMethod = "setup")
        @ConditionalOnProperty(name = KAFKA_NOTIFICATIONS_ENABLED_PROPERTY)
        public KafkaSetupHandler kafkaSetupHandler(NewTopicProperties newTopicProperties) {
            Objects.requireNonNull(notificationEventsTopic, getWarningMessageOnNotDefinedProp("enabled notification events", KAFKA_TOPIC_NAME_PROPERTY));
            Objects.requireNonNull(bootstrapServers, getWarningMessageOnNotDefinedProp("enabled notification events", KAFKA_BOOTSTRAP_SERVERS_PROPERTY));

            return Mockito.mock(KafkaSetupHandler.class);
        }

        @Bean
        @ConditionalOnProperty(name = KAFKA_NOTIFICATIONS_ENABLED_PROPERTY)
        public SchemaEventHandlerForKafka schemaEventHandlerForKafka(SchemaParserProperties parserProperties) {
            Objects.requireNonNull(notificationEventsTopic, getWarningMessageOnNotDefinedProp("enabled notification events", KAFKA_TOPIC_NAME_PROPERTY));

            return Mockito.mock(SchemaEventHandlerForKafka.class);
        }
    }

    public NotificationEvent<Schema> getDummySchemaEvent(int event, EventType eventType, String source) {
        log.info("Emitting event {}", event);
        Schema schema = new Schema();
        return NotificationEvent.<Schema>builder()
                .entity(schema)
                .source(source)
                .eventType(eventType)
                .build();
    }

    public NotificationEvent<Stream> getDummyStreamEvent(int event, EventType eventType, String source) {
        log.info("Emitting event {}", event);
        Stream schema = new Stream();
        return NotificationEvent.<Stream>builder()
                .entity(schema)
                .source(source)
                .eventType(eventType)
                .build();
    }
}