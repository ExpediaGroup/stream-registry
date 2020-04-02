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
import static com.expediagroup.streamplatform.streamregistry.data.ObjectNodeMapper.deserialise;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.confluent.kafka.streams.serdes.avro.TestSerializer;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.junit4.SpringRunner;

import com.expediagroup.streamplatform.streamregistry.avro.AvroEvent;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKey;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKeyType;
import com.expediagroup.streamplatform.streamregistry.core.events.config.NewTopicProperties;
import com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig;
import com.expediagroup.streamplatform.streamregistry.core.events.listeners.SchemaNotificationEventListener;
import com.expediagroup.streamplatform.streamregistry.data.ObjectNodeMapper;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.Tag;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;

@Slf4j
@RunWith(SpringRunner.class)
@EmbeddedKafka
@SpringBootTest(
    classes = NotificationEventListenerKafkaIntegrationTest.SpyListenerConfiguration.class,
    properties = {
        KAFKA_NOTIFICATIONS_ENABLED_PROPERTY + "=true",
        KAFKA_TOPIC_NAME_PROPERTY + "=" + NotificationEventListenerKafkaIntegrationTest.TEST_NOTIFICATION_TOPIC,
        KAFKA_TOPIC_SETUP_PROPERTY + "=true",
        KAFKA_TOPIC_SETUP_PROPERTY + ".numPartitions=" + NotificationEventListenerKafkaIntegrationTest.TEST_PARTITIONS,
        KAFKA_TOPIC_SETUP_PROPERTY + ".replicationFactor=" + NotificationEventListenerKafkaIntegrationTest.TEST_REPLICATION_FACTOR,
        KAFKA_BOOTSTRAP_SERVERS_PROPERTY + "=${spring.embedded.kafka.brokers}",
        KAFKA_SCHEMA_REGISTRY_URL_PROPERTY + "=" + NotificationEventListenerKafkaIntegrationTest.TEST_SCHEMA_REGISTRY
    })
public class NotificationEventListenerKafkaIntegrationTest {
  public static final String TEST_NOTIFICATION_TOPIC = "test-notification-topic";
  public static final String TEST_PARTITIONS = "2";
  public static final String TEST_REPLICATION_FACTOR = "1";
  public static final String TEST_SCHEMA_REGISTRY = "http://foo:8082";

  public static final int TEST_CREATE_SCHEMA_EVENTS = 5;
  public static final int TEST_UPDATE_SCHEMA_EVENTS = 3;

  public static final int TEST_CREATE_STREAM_EVENTS = 2;
  public static final int TEST_UPDATE_STREAM_EVENTS = 4;

  public static final Map<AvroKey, AvroEvent> producedEvents = new ConcurrentHashMap<>();

  @Value("${" + KAFKA_BOOTSTRAP_SERVERS_PROPERTY + "}")
  private String bootstrapServers;

  @Autowired
  private ApplicationEventMulticaster applicationEventMulticaster;

  @Autowired
  private KafkaSetupHandler kafkaSetupHandler;

  @BeforeClass
  public static void beforeClass() throws IOException, RestClientException {
    MockSchemaRegistryClient srClient = TestSerializer.schemaRegistryClient;

    srClient.register(getSubjectName(TEST_NOTIFICATION_TOPIC, true), AvroKey.getClassSchema());
    srClient.register(getSubjectName(TEST_NOTIFICATION_TOPIC, false), AvroEvent.getClassSchema());
  }

  @Test
  public void having_emitted_events_verify_that_Kafka_handler_is_producing_them() throws InterruptedException {
    IntStream.rangeClosed(1, TEST_CREATE_SCHEMA_EVENTS).forEachOrdered(i -> applicationEventMulticaster.multicastEvent(getDummySchemaEvent(i, EventType.CREATE, "schema-create")));
    IntStream.rangeClosed(1, TEST_UPDATE_SCHEMA_EVENTS).forEachOrdered(i -> applicationEventMulticaster.multicastEvent(getDummySchemaEvent(i, EventType.UPDATE, "schema-update")));

    IntStream.rangeClosed(1, TEST_CREATE_STREAM_EVENTS).forEachOrdered(i -> applicationEventMulticaster.multicastEvent(getDummyStreamEvent(i, EventType.CREATE, "stream-create")));
    IntStream.rangeClosed(1, TEST_UPDATE_STREAM_EVENTS).forEachOrdered(i -> applicationEventMulticaster.multicastEvent(getDummyStreamEvent(i, EventType.UPDATE, "stream-update")));

    TimeUnit.SECONDS.sleep(5);

    Assert.assertFalse("Produced events shouldn't be empty", producedEvents.isEmpty());

    val schemaEvents = producedEvents.keySet().stream().filter(key -> key.getType().equals(AvroKeyType.SCHEMA)).count();
    val streamEvents = producedEvents.keySet().stream().filter(key -> key.getType().equals(AvroKeyType.STREAM_VERSION)).count();

    Assert.assertEquals("Number of messages should be same as schema events", schemaEvents, (TEST_CREATE_SCHEMA_EVENTS + TEST_UPDATE_SCHEMA_EVENTS));
    Assert.assertEquals("Number of messages should be same as stream events", streamEvents, (TEST_CREATE_STREAM_EVENTS + TEST_UPDATE_STREAM_EVENTS));

    val streamsWithSchemaKey = producedEvents.values()
        .stream()
        .map(AvroEvent::getStreamEntity)
        .filter(Objects::nonNull)
        .filter(st -> st.getSchemaKey() != null)
        .filter(st -> st.getSchemaKey().getId().startsWith(st.getName()))
        .count();

    Assert.assertEquals("Stream messages should contain a schema key with stream name as id prefix", streamsWithSchemaKey, (TEST_CREATE_STREAM_EVENTS + TEST_UPDATE_STREAM_EVENTS));
  }

  @Test
  public void having_topic_setup_enabled_verify_that_notification_topic_is_being_created() throws ExecutionException, InterruptedException {
    Mockito.verify(kafkaSetupHandler, Mockito.timeout(5000).times(1)).setup();

    try (AdminClient client = createAdminClient()) {
      Optional<TopicDescription> desc = obtainTopicDescription(client, TEST_NOTIFICATION_TOPIC);
      Assert.assertTrue(String.format("Topic %s should be present", TEST_NOTIFICATION_TOPIC), desc.isPresent());
    }
  }

  @Slf4j
  @Configuration
  @ComponentScan(basePackageClasses = {SchemaNotificationEventListener.class})
  public static class SpyListenerConfiguration extends NotificationEventConfig {
    @Value("${" + KAFKA_BOOTSTRAP_SERVERS_PROPERTY + ":#{null}}")
    private String bootstrapServers;

    @Bean
    public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
      SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
      eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());

      return eventMulticaster;
    }

    public ProducerFactory<SpecificRecord, SpecificRecord> producerFactory() {
      Objects.requireNonNull(bootstrapServers, getWarningMessageOnNotDefinedProp("enabled notification events", KAFKA_BOOTSTRAP_SERVERS_PROPERTY));

      Map<String, Object> props = new HashMap<>();
      props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
      props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, TEST_SCHEMA_REGISTRY);
      props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, TestSerializer.class);
      props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, TestSerializer.class);

      return new DefaultKafkaProducerFactory<>(props);
    }

    public KafkaSetupHandler kafkaSetupHandler(NewTopicProperties newTopicProperties) {
      return Mockito.spy(super.kafkaSetupHandler(newTopicProperties));
    }

    public KafkaTemplate<SpecificRecord, SpecificRecord> kafkaTemplate() {
      KafkaTemplate<SpecificRecord, SpecificRecord> template = new KafkaTemplate<>(producerFactory());

      template.setProducerListener(new ProducerListener<>() {
        @Override
        public void onSuccess(ProducerRecord<SpecificRecord, SpecificRecord> producerRecord, RecordMetadata recordMetadata) {
          log.info("Produced record {}", producerRecord);
          producedEvents.put((AvroKey) producerRecord.key(), (AvroEvent) producerRecord.value());
        }
      });

      return template;
    }
  }

  private Optional<TopicDescription> obtainTopicDescription(AdminClient client, String topic) throws ExecutionException, InterruptedException {
    try {
      log.info("Verifying existence of topic {}", topic);

      return Optional.ofNullable(client.describeTopics(Collections.singleton(topic)).all().get().get(topic));
    } catch (ExecutionException exception) {
      if (exception.getCause() != null && exception.getCause() instanceof UnknownTopicOrPartitionException) {
        return Optional.empty();
      } else throw exception;
    }
  }

  private AdminClient createAdminClient() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

    log.info("Creating a Kafka Admin client with configuration {}", configs);

    return AdminClient.create(configs);
  }

  public static String getSubjectName(String topic, boolean isKey) {
    return isKey ? topic + "-key" : topic + "-value";
  }

  public NotificationEvent<Schema> getDummySchemaEvent(int event, EventType eventType, String source) {
    log.info("Emitting event {}", event);
    Schema schema = getDummySchema();
    return NotificationEvent.<Schema>builder()
        .entity(schema)
        .source(source)
        .eventType(eventType)
        .build();
  }

  public static Schema getDummySchema() {
    String name = Instant.now().toString();
    String domain = "domain";
    String description = "description";
    String type = "type";
    String configJson = "{}";
    String statusJson = "{foo:bar}";
    List<Tag> tags = Collections.singletonList(new Tag("tag-name", "tag-value"));

    SchemaKey key = new SchemaKey();
    key.setName(name);
    key.setDomain(domain);

    Specification spec = new Specification(description,tags,type,deserialise(configJson));

    Status status = new Status(deserialise(statusJson));

    Schema schema = new Schema();
    schema.setKey(key);
    schema.setSpecification(spec);
    schema.setStatus(status);

    return schema;
  }

  public NotificationEvent<Stream> getDummyStreamEvent(int event, EventType eventType, String source) {
    log.info("Emitting event {}", event);
    Stream stream = getDummyStream();
    return NotificationEvent.<Stream>builder()
        .entity(stream)
        .source(source)
        .eventType(eventType)
        .build();
  }

  public static Stream getDummyStream() {
    String name = Instant.now().toString();
    String domain = "domain";
    String description = "description";
    String type = "type";
    String configJson = "{}";
    String statusJson = "{foo:bar}";
    List<Tag> tags = Collections.singletonList(new Tag("tag-name", "tag-value"));
    int version = 1;

    StreamKey key = new StreamKey();
    key.setName(name);
    key.setDomain(domain);
    key.setVersion(version);

    Specification spec = new Specification(description,tags,type,deserialise(configJson));

    Status status = new Status(deserialise(statusJson));

    Stream stream = new Stream();
    stream.setKey(key);
    stream.setSpecification(spec);
    stream.setStatus(status);

    SchemaKey schemaKey = new SchemaKey();
    schemaKey.setName(stream.getKey().getName().concat("_v2"));
    schemaKey.setDomain(domain);
    stream.setSchemaKey(schemaKey);

    return stream;
  }
}