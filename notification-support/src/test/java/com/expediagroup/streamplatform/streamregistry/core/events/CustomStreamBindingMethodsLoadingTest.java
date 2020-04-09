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
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.CUSTOM_STREAM_BINDING_KEY_PARSER_CLASS_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.CUSTOM_STREAM_BINDING_KEY_PARSER_METHOD_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.CUSTOM_STREAM_BINDING_PARSER_ENABLED_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.CUSTOM_STREAM_BINDING_VALUE_PARSER_CLASS_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.CUSTOM_STREAM_BINDING_VALUE_PARSER_METHOD_PROPERTY;
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
import com.expediagroup.streamplatform.streamregistry.avro.AvroStreamBinding;
import com.expediagroup.streamplatform.streamregistry.core.events.config.NewTopicProperties;
import com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig;
import com.expediagroup.streamplatform.streamregistry.core.events.handlers.StreamBindingEventHandlerForKafka;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.Tag;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamBindingKey;

@RunWith(SpringRunner.class)// Explicitly defined prop with true as value
@SpringBootTest(classes = CustomStreamBindingMethodsLoadingTest.MockListenerConfiguration.class,
    properties = {
        KAFKA_NOTIFICATIONS_ENABLED_PROPERTY + "=true",
        KAFKA_TOPIC_NAME_PROPERTY + "=my-topic",
        KAFKA_TOPIC_SETUP_PROPERTY + "=false", // We don't test setup topic here but in  the integration test
        KAFKA_BOOTSTRAP_SERVERS_PROPERTY + "=localhost:9092",
        KAFKA_SCHEMA_REGISTRY_URL_PROPERTY + "=foo:8081",
        CUSTOM_STREAM_BINDING_PARSER_ENABLED_PROPERTY + "=true",
        CUSTOM_STREAM_BINDING_KEY_PARSER_CLASS_PROPERTY + "=com.expediagroup.streamplatform.streamregistry.core.events.CustomStreamBindingMethodsLoadingTest",
        CUSTOM_STREAM_BINDING_KEY_PARSER_METHOD_PROPERTY + "=myCustomKey",
        CUSTOM_STREAM_BINDING_VALUE_PARSER_CLASS_PROPERTY + "=com.expediagroup.streamplatform.streamregistry.core.events.CustomStreamBindingMethodsLoadingTest",
        CUSTOM_STREAM_BINDING_VALUE_PARSER_METHOD_PROPERTY + "=myCustomEvent"
    })
public class CustomStreamBindingMethodsLoadingTest {
  private static final AtomicReference<AvroKey> testAvroKeyResult = new AtomicReference<>();
  private static final AtomicReference<AvroEvent> testAvroEventResult = new AtomicReference<>();

  @Autowired
  private StreamBindingEventHandlerForKafka streamBindingEventHandlerForKafka;

  @Before
  public void before() {
    testAvroKeyResult.set(null);
    testAvroEventResult.set(null);
  }

  @Test
  public void having_defined_custom_parser_methods_verify_they_execute_properly() {
    val avrokey = (AvroKey) streamBindingEventHandlerForKafka.getStreamBindingToKeyRecord().apply(getDummyStreamBinding());
    val avroEvent = (AvroEvent) streamBindingEventHandlerForKafka.getStreamBindingToValueRecord().apply(getDummyStreamBinding());

    Assert.assertNotNull("Avro key shouldn't be null", avrokey);
    Assert.assertNotNull("Avro event shouldn't be null", avroEvent);
    Assert.assertNotNull("StreamBinding entity shouldn't be null", avroEvent.getStreamBindingEntity());

    Assert.assertEquals(avrokey, testAvroKeyResult.get());
    Assert.assertEquals(avroEvent, testAvroEventResult.get());
  }

  public static AvroKey myCustomKey(StreamBinding streamBinding) {
    val name = streamBinding.getKey().getStreamName();
    val version = streamBinding.getKey().getStreamVersion();
    val domainName = streamBinding.getKey().getStreamDomain();
    val infrastructureName = streamBinding.getKey().getInfrastructureName();
    val infrastructureZone = streamBinding.getKey().getInfrastructureZone();

    var domainKey = AvroKey.newBuilder()
        .setId(domainName)
        .setType(AvroKeyType.DOMAIN)
        .build();

    var zoneKey = AvroKey.newBuilder()
        .setId(infrastructureZone)
        .setParent(domainKey)
        .setType(AvroKeyType.ZONE)
        .build();

    var infrastructureKey = AvroKey.newBuilder()
        .setId(infrastructureName)
        .setParent(zoneKey)
        .setType(AvroKeyType.INFRASTRUCTURE)
        .build();

    var streamKey = AvroKey.newBuilder()
        .setId(name)
        .setParent(domainKey)
        .setType(AvroKeyType.STREAM)
        .build();

    AvroKey avroKey = AvroKey.newBuilder()
        .setId(version.toString())
        .setParent(streamKey)
        .setPhysical(infrastructureKey)
        .setType(AvroKeyType.STREAM_VERSION)
        .build();

    testAvroKeyResult.set(avroKey);

    return avroKey;
  }

  public static AvroEvent myCustomEvent(StreamBinding streamBinding) {

    val avroEvent = AvroStreamBinding.newBuilder()
        .setStreamDomain(streamBinding.getKey().getStreamDomain())
        .setStreamVersion(streamBinding.getKey().getStreamVersion())
        .setStreamName(streamBinding.getKey().getStreamName())
        .setInfrastructureZone(streamBinding.getKey().getInfrastructureZone())
        .setInfrastructureName(streamBinding.getKey().getInfrastructureName())
        .setDescription(streamBinding.getSpecification().getDescription())
        .setTags(Collections.emptyList())
        .setType(streamBinding.getSpecification().getType())
        .setConfigurationString(streamBinding.getSpecification().getConfigJson())
        .setStatusString(streamBinding.getStatus().getStatusJson())
        .build();

    val event = AvroEvent.newBuilder().setStreamBindingEntity(avroEvent).build();

    testAvroEventResult.set(event);

    return event;
  }

  public static StreamBinding getDummyStreamBinding() {
    val name = Instant.now().toString();
    val domain = "domain";
    val description = "description";
    val type = "type";
    val configJson = "{}";
    val statusJson = "{foo:bar}";
    val tags = Collections.singletonList(new Tag("tag-name", "tag-value"));
    val version = 1;
    val zone = "aws_us_east_1";
    val infrastructureName = "kafka_1a";

    // Key
    val key = new StreamBindingKey();
    key.setStreamName(name);
    key.setStreamDomain(domain);
    key.setStreamVersion(version);
    key.setInfrastructureName(infrastructureName);
    key.setInfrastructureZone(zone);

    // Spec
    val spec = new Specification();
    spec.setDescription(description);
    spec.setType(type);
    spec.setConfigJson(configJson);
    spec.setTags(tags);

    // Status
    val status = new Status();
    status.setStatusJson(statusJson);

    val streamBinding = new StreamBinding();
    streamBinding.setKey(key);
    streamBinding.setSpecification(spec);
    streamBinding.setStatus(status);

    return streamBinding;
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