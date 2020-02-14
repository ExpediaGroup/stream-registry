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

import static com.expediagroup.streamplatform.streamregistry.core.events.KafkaNotificationListenerConfig.KAFKA_BOOTSTRAP_SERVERS_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.KafkaNotificationListenerConfig.KAFKA_NOTIFICATIONS_ENABLED_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.KafkaNotificationListenerConfig.KAFKA_SCHEMA_REGISTRY_URL_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.KafkaNotificationListenerConfig.KAFKA_TOPIC_NAME_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.KafkaNotificationListenerConfig.KAFKA_TOPIC_SETUP_PROPERTY;

import java.util.stream.IntStream;

import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;

import com.expediagroup.streamplatform.streamregistry.model.Schema;

@Slf4j
@RunWith(SpringRunner.class)// Explicitly defined prop with true as value
@SpringBootTest(classes = {MockListenerConfiguration.class, KafkaNotificationEventListenerTest.MulticasterConfig.class},
        properties = {
                KAFKA_NOTIFICATIONS_ENABLED_PROPERTY + "=true",
                KAFKA_TOPIC_NAME_PROPERTY + "=my-topic",
                KAFKA_TOPIC_SETUP_PROPERTY + "=false", // We don't test setup topic here but in  the integration test
                KAFKA_BOOTSTRAP_SERVERS_PROPERTY + "=localhost:9092",
                KAFKA_SCHEMA_REGISTRY_URL_PROPERTY + "=foo:8081"})
public class KafkaNotificationEventListenerTest {
    public static final int TEST_CREATE_EVENTS = 5;
    public static final int TEST_UPDATE_EVENTS = 3;

    @Autowired
    private ApplicationEventMulticaster applicationEventMulticaster;

    @Autowired
    private KafkaNotificationEventListener kafkaNotificationEventListener;

    @Before
    public void before() {
        IntStream.rangeClosed(1, TEST_CREATE_EVENTS).forEachOrdered(i -> applicationEventMulticaster.multicastEvent(getDummy(i, EventType.CREATE, "schema-create"))); // [1..5]
        IntStream.rangeClosed(1, TEST_UPDATE_EVENTS).forEachOrdered(i -> applicationEventMulticaster.multicastEvent(getDummy(i, EventType.UPDATE, "schema-update"))); // [1..5]
    }

    @Test
    public void having_pushed_events_verify_that_listener_methods_are_executed() {
        Mockito.verify(kafkaNotificationEventListener, Mockito.timeout(5000).times(TEST_CREATE_EVENTS)).onCreateSchema(Mockito.notNull());
        Mockito.verify(kafkaNotificationEventListener, Mockito.timeout(5000).times(TEST_UPDATE_EVENTS)).onUpdateSchema(Mockito.notNull());
    }

    @Configuration
    public static class MulticasterConfig {
        @Bean(name = "applicationEventMulticaster")
        public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
            SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
            eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());

            return eventMulticaster;
        }
    }

    public NotificationEvent<Schema> getDummy(int event, EventType eventType, String source) {
        log.info("Emitting event {}", event);
        Schema schema = new Schema();
        return NotificationEvent.<Schema>builder()
                .entity(schema)
                .source(source)
                .eventType(eventType)
                .build();
    }
}