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
package com.expediagroup.streamplatform.streamregistry.core.events.config;

import static com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventUtils.getWarningMessageOnNotDefinedProp;

import java.util.HashMap;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.expediagroup.streamplatform.streamregistry.core.events.KafkaSetupHandler;
import com.expediagroup.streamplatform.streamregistry.core.events.handlers.ProducerEventHandlerForKafka;
import com.expediagroup.streamplatform.streamregistry.core.events.handlers.SchemaEventHandlerForKafka;
import com.expediagroup.streamplatform.streamregistry.core.events.handlers.StreamBindingEventHandlerForKafka;
import com.expediagroup.streamplatform.streamregistry.core.events.handlers.StreamEventHandlerForKafka;

@Slf4j
@Configuration
@EnableConfigurationProperties({NewTopicProperties.class, SchemaParserProperties.class, StreamParserProperties.class, StreamBindingParserProperties.class, ProducerParserProperties.class})
public class NotificationEventConfig {
  public static final String KAFKA_SCHEMA_REGISTRY_URL_PROPERTY = "notification.events.kafka.schema.registry.url";
  public static final String KAFKA_BOOTSTRAP_SERVERS_PROPERTY = "notification.events.kafka.bootstrap-servers";
  public static final String KAFKA_NOTIFICATIONS_ENABLED_PROPERTY = "notification.events.kafka.enabled";
  public static final String KAFKA_TOPIC_NAME_PROPERTY = "notification.events.kafka.topic";
  public static final String KAFKA_TOPIC_SETUP_PROPERTY = "notification.events.kafka.topic.setup";

  public static final String CUSTOM_SCHEMA_TYPE_PREFIX = "notification.events.kafka.custom.schema";
  public static final String CUSTOM_SCHEMA_PARSER_ENABLED_PROPERTY = "notification.events.kafka.custom.schema.custom-enabled";
  public static final String CUSTOM_SCHEMA_KEY_PARSER_CLASS_PROPERTY = "notification.events.kafka.custom.schema.key-parser-class";
  public static final String CUSTOM_SCHEMA_KEY_PARSER_METHOD_PROPERTY = "notification.events.kafka.custom.schema.key-parser-method";
  public static final String CUSTOM_SCHEMA_VALUE_PARSER_CLASS_PROPERTY = "notification.events.kafka.custom.schema.value-parser-class";
  public static final String CUSTOM_SCHEMA_VALUE_PARSER_METHOD_PROPERTY = "notification.events.kafka.custom.schema.value-parser-method";

  public static final String CUSTOM_STREAM_TYPE_PREFIX = "notification.events.kafka.custom.stream";
  public static final String CUSTOM_STREAM_PARSER_ENABLED_PROPERTY = "notification.events.kafka.custom.stream.custom-enabled";
  public static final String CUSTOM_STREAM_KEY_PARSER_CLASS_PROPERTY = "notification.events.kafka.custom.stream.key-parser-class";
  public static final String CUSTOM_STREAM_KEY_PARSER_METHOD_PROPERTY = "notification.events.kafka.custom.stream.key-parser-method";
  public static final String CUSTOM_STREAM_VALUE_PARSER_CLASS_PROPERTY = "notification.events.kafka.custom.stream.value-parser-class";
  public static final String CUSTOM_STREAM_VALUE_PARSER_METHOD_PROPERTY = "notification.events.kafka.custom.stream.value-parser-method";

  public static final String CUSTOM_STREAM_BINDING_TYPE_PREFIX = "notification.events.kafka.custom.stream-binding";
  public static final String CUSTOM_STREAM_BINDING_PARSER_ENABLED_PROPERTY = "notification.events.kafka.custom.stream-binding.custom-enabled";
  public static final String CUSTOM_STREAM_BINDING_KEY_PARSER_CLASS_PROPERTY = "notification.events.kafka.custom.stream-binding.key-parser-class";
  public static final String CUSTOM_STREAM_BINDING_KEY_PARSER_METHOD_PROPERTY = "notification.events.kafka.custom.stream-binding.key-parser-method";
  public static final String CUSTOM_STREAM_BINDING_VALUE_PARSER_CLASS_PROPERTY = "notification.events.kafka.custom.stream-binding.value-parser-class";
  public static final String CUSTOM_STREAM_BINDING_VALUE_PARSER_METHOD_PROPERTY = "notification.events.kafka.custom.stream-binding.value-parser-method";

  public static final String CUSTOM_PRODUCER_TYPE_PREFIX = "notification.events.kafka.custom.producer";
  public static final String CUSTOM_PRODUCER_PARSER_ENABLED_PROPERTY = "notification.events.kafka.custom.producer.custom-enabled";
  public static final String CUSTOM_PRODUCER_KEY_PARSER_CLASS_PROPERTY = "notification.events.kafka.custom.producer.key-parser-class";
  public static final String CUSTOM_PRODUCER_KEY_PARSER_METHOD_PROPERTY = "notification.events.kafka.custom.producer.key-parser-method";
  public static final String CUSTOM_PRODUCER_VALUE_PARSER_CLASS_PROPERTY = "notification.events.kafka.custom.producer.value-parser-class";
  public static final String CUSTOM_PRODUCER_VALUE_PARSER_METHOD_PROPERTY = "notification.events.kafka.custom.producer.value-parser-method";

  @Value("${" + KAFKA_TOPIC_SETUP_PROPERTY + ":false}")
  private Boolean isKafkaSetupEnabled;

  @Value("${" + KAFKA_TOPIC_NAME_PROPERTY + ":#{null}}")
  private String notificationEventsTopic;

  @Value("${" + KAFKA_BOOTSTRAP_SERVERS_PROPERTY + ":#{null}}")
  private String bootstrapServers;

  @Value("${" + KAFKA_SCHEMA_REGISTRY_URL_PROPERTY + ":#{null}}")
  private String schemaRegistryUrl;

  @Bean
  @ConditionalOnProperty(name = KAFKA_NOTIFICATIONS_ENABLED_PROPERTY)
  public ProducerFactory<SpecificRecord, SpecificRecord> producerFactory() {
    log.info("Building kafka producer in cluster {} with schema registry {}", bootstrapServers, schemaRegistryUrl);
    Objects.requireNonNull(bootstrapServers, getWarningMessageOnNotDefinedProp("enabled notification events", KAFKA_BOOTSTRAP_SERVERS_PROPERTY));
    Objects.requireNonNull(schemaRegistryUrl, getWarningMessageOnNotDefinedProp("enabled notification events", KAFKA_SCHEMA_REGISTRY_URL_PROPERTY));

    val props = new HashMap<String, Object>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

    return new DefaultKafkaProducerFactory<>(props);
  }

  @Bean
  @ConditionalOnProperty(name = KAFKA_NOTIFICATIONS_ENABLED_PROPERTY)
  public KafkaTemplate<SpecificRecord, SpecificRecord> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }

  @Bean
  @ConditionalOnProperty(name = KAFKA_NOTIFICATIONS_ENABLED_PROPERTY)
  public SchemaEventHandlerForKafka schemaEventHandlerForKafka(SchemaParserProperties parserProperties) {
    Objects.requireNonNull(notificationEventsTopic, getWarningMessageOnNotDefinedProp("enabled notification events", KAFKA_TOPIC_NAME_PROPERTY));

    return SchemaEventHandlerForKafka.builder()
        .notificationEventsTopic(notificationEventsTopic)
        .schemaToKeyRecord(parserProperties.buildSchemaToKeyRecord())
        .schemaToValueRecord(parserProperties.buildSchemaToValueRecord())
        .kafkaTemplate(kafkaTemplate())
        .build();
  }

  @Bean
  @ConditionalOnProperty(name = KAFKA_NOTIFICATIONS_ENABLED_PROPERTY)
  public StreamEventHandlerForKafka streamEventHandlerForKafka(StreamParserProperties parserProperties) {
    Objects.requireNonNull(notificationEventsTopic, getWarningMessageOnNotDefinedProp("enabled notification events", KAFKA_TOPIC_NAME_PROPERTY));

    return StreamEventHandlerForKafka.builder()
        .notificationEventsTopic(notificationEventsTopic)
        .streamToKeyRecord(parserProperties.buildStreamToKeyRecord())
        .streamToValueRecord(parserProperties.buildStreamToValueRecord())
        .kafkaTemplate(kafkaTemplate())
        .build();
  }

  @Bean
  @ConditionalOnProperty(name = KAFKA_NOTIFICATIONS_ENABLED_PROPERTY)
  public StreamBindingEventHandlerForKafka streamBindingEventHandlerForKafka(StreamBindingParserProperties parserProperties) {
    Objects.requireNonNull(notificationEventsTopic, getWarningMessageOnNotDefinedProp("enabled notification events", KAFKA_TOPIC_NAME_PROPERTY));

    return StreamBindingEventHandlerForKafka.builder()
        .notificationEventsTopic(notificationEventsTopic)
        .streamBindingToKeyRecord(parserProperties.buildStreamBindingToKeyRecord())
        .streamBindingToValueRecord(parserProperties.buildStreamBindingToValueRecord())
        .kafkaTemplate(kafkaTemplate())
        .build();
  }

  @Bean
  @ConditionalOnProperty(name = KAFKA_NOTIFICATIONS_ENABLED_PROPERTY)
  public ProducerEventHandlerForKafka producerEventHandlerForKafka(ProducerParserProperties parserProperties) {
    Objects.requireNonNull(notificationEventsTopic, getWarningMessageOnNotDefinedProp("enabled notification events", KAFKA_TOPIC_NAME_PROPERTY));

    return ProducerEventHandlerForKafka.builder()
        .notificationEventsTopic(notificationEventsTopic)
        .producerToKeyRecord(parserProperties.buildProducerToKeyRecord())
        .producerToValueRecord(parserProperties.buildProducerToValueRecord())
        .kafkaTemplate(kafkaTemplate())
        .build();
  }

  @Bean(initMethod = "setup")
  @ConditionalOnProperty(name = KAFKA_NOTIFICATIONS_ENABLED_PROPERTY)
  public KafkaSetupHandler kafkaSetupHandler(NewTopicProperties newTopicProperties) {
    Objects.requireNonNull(notificationEventsTopic, getWarningMessageOnNotDefinedProp("enabled notification events", KAFKA_TOPIC_NAME_PROPERTY));
    Objects.requireNonNull(bootstrapServers, getWarningMessageOnNotDefinedProp("enabled notification events", KAFKA_BOOTSTRAP_SERVERS_PROPERTY));

    val newTopic = isKafkaSetupEnabled ? newTopicProperties.buildNewTopic(notificationEventsTopic) : null;

    return KafkaSetupHandler.builder()
        .newTopic(newTopic)
        .notificationEventsTopic(notificationEventsTopic)
        .bootstrapServers(bootstrapServers)
        .kafkaSetupEnabled(isKafkaSetupEnabled)
        .build();
  }
}
