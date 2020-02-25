/**
 * Copyright (C) 2018-2020 Expedia, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.core.events;

import static com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventConfig.*;

import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import lombok.val;

import org.junit.Assert;
import org.junit.Before;
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

import com.expediagroup.streamplatform.streamregistry.avro.AvroEvent;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKey;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKeyType;
import com.expediagroup.streamplatform.streamregistry.avro.AvroSchema;
import com.expediagroup.streamplatform.streamregistry.core.events.handlers.SchemaEventHandlerForKafka;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Tag;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;

@RunWith(SpringRunner.class)// Explicitly defined prop with true as value
@SpringBootTest(classes = CustomSchemaMethodsLoadingTest.MockListenerConfiguration.class,
        properties = {
                KAFKA_NOTIFICATIONS_ENABLED_PROPERTY + "=true",
                KAFKA_TOPIC_NAME_PROPERTY + "=my-topic",
                KAFKA_TOPIC_SETUP_PROPERTY + "=false", // We don't test setup topic here but in  the integration test
                KAFKA_BOOTSTRAP_SERVERS_PROPERTY + "=localhost:9092",
                KAFKA_SCHEMA_REGISTRY_URL_PROPERTY + "=foo:8081",
                CUSTOM_SCHEMA_PARSER_ENABLED_PROPERTY + "=true",
                CUSTOM_SCHEMA_KEY_PARSER_CLASS_PROPERTY + "=com.expediagroup.streamplatform.streamregistry.core.events.CustomSchemaMethodsLoadingTest",
                CUSTOM_SCHEMA_KEY_PARSER_METHOD_PROPERTY + "=myCustomKey",
                CUSTOM_SCHEMA_VALUE_PARSER_CLASS_PROPERTY + "=com.expediagroup.streamplatform.streamregistry.core.events.CustomSchemaMethodsLoadingTest",
                CUSTOM_SCHEMA_VALUE_PARSER_METHOD_PROPERTY + "=myCustomEvent"
        })
public class CustomSchemaMethodsLoadingTest {
    private static final AtomicReference<AvroKey> testAvroKeyResult = new AtomicReference<>();
    private static final AtomicReference<AvroEvent> testAvroEventResult = new AtomicReference<>();

    @Autowired
    private SchemaEventHandlerForKafka schemaEventHandlerForKafka;

    @Before
    public void before() {
        testAvroKeyResult.set(null);
        testAvroEventResult.set(null);
    }

    @Test
    public void having_defined_custom_parser_methods_verify_they_execute_properly() {
        val avrokey = (AvroKey) schemaEventHandlerForKafka.getSchemaToKeyRecord().apply(getDummySchema());
        val avroEvent = (AvroEvent) schemaEventHandlerForKafka.getSchemaToValueRecord().apply(getDummySchema());

        Assert.assertEquals(avrokey, testAvroKeyResult.get());
        Assert.assertEquals(avroEvent, testAvroEventResult.get());
    }

    public static AvroKey myCustomKey(Schema schema) {
        val key = AvroKey.newBuilder()
                .setId(schema.getKey().getName())
                .setDomain(schema.getKey().getDomain())
                .setVersion(null)
                .setParent(null)
                .setType(AvroKeyType.SCHEMA)
                .build();

        testAvroKeyResult.set(key);

        return key;
    }

    public static AvroEvent myCustomEvent(Schema schema) {
        val avroEvent = AvroSchema.newBuilder()
                .setDomain(schema.getKey().getDomain())
                .setName(schema.getKey().getName())
                .setDescription(schema.getSpecification().getDescription())
                .setTags(Collections.emptyList())
                .setType(schema.getSpecification().getType())
                .setConfigurationString(schema.getSpecification().getConfigJson())
                .setStatusString(schema.getStatus().getStatusJson())
                .build();

        val event = AvroEvent.newBuilder().setSchemaEntity(avroEvent).build();

        testAvroEventResult.set(event);

        return event;
    }

    public static Schema getDummySchema() {
        val name = Instant.now().toString();
        val domain = "domain";
        val description = "description";
        val type = "type";
        val configJson = "{}";
        val statusJson = "{foo:bar}";
        val tags = Collections.singletonList(new Tag("tag-name", "tag-value"));

        // Key
        val key = new SchemaKey();
        key.setName(name);
        key.setDomain(domain);

        // Spec
        val spec = new Specification();
        spec.setDescription(description);
        spec.setType(type);
        spec.setConfigJson(configJson);
        spec.setTags(tags);

        // Status
        val status = new Status();
        status.setStatusJson(statusJson);

        val schema = new Schema();
        schema.setKey(key);
        schema.setSpecification(spec);
        schema.setStatus(status);

        return schema;
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
