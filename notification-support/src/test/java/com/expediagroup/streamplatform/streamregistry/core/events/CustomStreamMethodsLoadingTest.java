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
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.CUSTOM_STREAM_KEY_PARSER_CLASS_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.CUSTOM_STREAM_KEY_PARSER_METHOD_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.CUSTOM_STREAM_PARSER_ENABLED_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.CUSTOM_STREAM_VALUE_PARSER_CLASS_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.CUSTOM_STREAM_VALUE_PARSER_METHOD_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.KAFKA_BOOTSTRAP_SERVERS_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.KAFKA_NOTIFICATIONS_ENABLED_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.KAFKA_SCHEMA_REGISTRY_URL_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.KAFKA_TOPIC_NAME_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.KAFKA_TOPIC_SETUP_PROPERTY;

import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import lombok.val;

import org.apache.avro.specific.SpecificRecord;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.junit4.SpringRunner;

import com.expediagroup.streamplatform.streamregistry.avro.AvroEvent;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKey;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKeyType;
import com.expediagroup.streamplatform.streamregistry.avro.AvroStream;
import com.expediagroup.streamplatform.streamregistry.core.events.config.NewTopicProperties;
import com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig;
import com.expediagroup.streamplatform.streamregistry.core.events.handlers.StreamEventHandlerForKafka;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.Tag;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;

@RunWith(SpringRunner.class)// Explicitly defined prop with true as value
@SpringBootTest(classes = CustomStreamMethodsLoadingTest.MockListenerConfiguration.class,
    properties = {
        KAFKA_NOTIFICATIONS_ENABLED_PROPERTY + "=true",
        KAFKA_TOPIC_NAME_PROPERTY + "=my-topic",
        KAFKA_TOPIC_SETUP_PROPERTY + "=false", // We don't test setup topic here but in  the integration test
        KAFKA_BOOTSTRAP_SERVERS_PROPERTY + "=localhost:9092",
        KAFKA_SCHEMA_REGISTRY_URL_PROPERTY + "=foo:8081",
        CUSTOM_STREAM_PARSER_ENABLED_PROPERTY + "=true",
        CUSTOM_STREAM_KEY_PARSER_CLASS_PROPERTY + "=com.expediagroup.streamplatform.streamregistry.core.events.CustomStreamMethodsLoadingTest",
        CUSTOM_STREAM_KEY_PARSER_METHOD_PROPERTY + "=myCustomKey",
        CUSTOM_STREAM_VALUE_PARSER_CLASS_PROPERTY + "=com.expediagroup.streamplatform.streamregistry.core.events.CustomStreamMethodsLoadingTest",
        CUSTOM_STREAM_VALUE_PARSER_METHOD_PROPERTY + "=myCustomEvent"
    })
public class CustomStreamMethodsLoadingTest {
  private static final AtomicReference<AvroKey> testAvroKeyResult = new AtomicReference<>();
  private static final AtomicReference<AvroEvent> testAvroEventResult = new AtomicReference<>();

  @Autowired
  private StreamEventHandlerForKafka streamEventHandlerForKafka;

  @Before
  public void before() {
    testAvroKeyResult.set(null);
    testAvroEventResult.set(null);
  }

  @Test
  public void having_defined_custom_parser_methods_verify_they_execute_properly() {
    val avrokey = (AvroKey) streamEventHandlerForKafka.getStreamToKeyRecord().apply(getDummyStream());
    val avroEvent = (AvroEvent) streamEventHandlerForKafka.getStreamToValueRecord().apply(getDummyStream());

    Assert.assertNotNull("Avro key shouldn't be null", avrokey);
    Assert.assertNotNull("Avro event shouldn't be null", avroEvent);
    Assert.assertNotNull("Stream entity shouldn't be null", avroEvent.getStreamEntity());
    Assert.assertNotNull("Stream' schema-key shouldn't be null", avroEvent.getStreamEntity().getSchemaKey());

    Assert.assertEquals(avrokey, testAvroKeyResult.get());
    Assert.assertEquals(avroEvent, testAvroEventResult.get());
  }

  public static AvroKey myCustomKey(Stream stream) {
    val name = stream.getKey().getName();
    val version = stream.getKey().getVersion();
    val domainName = stream.getKey().getDomain();

    var domainKey = AvroKey.newBuilder()
        .setId(domainName)
        .setType(AvroKeyType.DOMAIN)
        .setParent(null)
        .build();

    var streamKey = AvroKey.newBuilder()
        .setId(name)
        .setParent(domainKey)
        .setType(AvroKeyType.STREAM)
        .build();

    val key = AvroKey.newBuilder()
        .setId(version.toString())
        .setParent(streamKey)
        .setType(AvroKeyType.STREAM_VERSION)
        .build();

    testAvroKeyResult.set(key);

    return key;
  }

  public static AvroEvent myCustomEvent(Stream stream) {
    val schemaKey = toAvroKeyRecord(stream.getSchemaKey());

    val avroEvent = AvroStream.newBuilder()
        .setDomain(stream.getKey().getDomain())
        .setVersion(stream.getKey().getVersion())
        .setName(stream.getKey().getName())
        .setDescription(stream.getSpecification().getDescription())
        .setTags(Collections.emptyList())
        .setType(stream.getSpecification().getType())
        .setConfigurationString(stream.getSpecification().getConfigJson())
        .setStatusString(stream.getStatus().getStatusJson())
        .setSchemaKey(schemaKey)
        .build();

    val event = AvroEvent.newBuilder().setStreamEntity(avroEvent).build();

    testAvroEventResult.set(event);

    return event;
  }

  public static AvroKey toAvroKeyRecord(SchemaKey schemaKey) {
    var domain = AvroKey.newBuilder()
        .setId(schemaKey.getDomain())
        .setType(AvroKeyType.DOMAIN)
        .setParent(null)
        .build();

    return AvroKey.newBuilder()
        .setId(schemaKey.getName())
        .setParent(domain)
        .setType(AvroKeyType.SCHEMA)
        .build();
  }

  public static Stream getDummyStream() {
    val name = Instant.now().toString();
    val domain = "domain";
    val description = "description";
    val type = "type";
    val configJson = "{}";
    val statusJson = "{foo:bar}";
    val tags = Collections.singletonList(new Tag("tag-name", "tag-value"));
    val version = 1;

    // Key
    val key = new StreamKey();
    key.setName(name);
    key.setDomain(domain);
    key.setVersion(version);

    // Spec
    val spec = new Specification();
    spec.setDescription(description);
    spec.setType(type);
    spec.setConfigJson(configJson);
    spec.setTags(tags);

    // Status
    val status = new Status();
    status.setStatusJson(statusJson);

    val stream = new Stream();
    stream.setKey(key);
    stream.setSpecification(spec);
    stream.setStatus(status);

    val schemaKey = new SchemaKey();
    schemaKey.setName(stream.getKey().getName().concat("_v1"));
    schemaKey.setDomain(domain);
    stream.setSchemaKey(schemaKey);

    return stream;
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