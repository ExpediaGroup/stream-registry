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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import com.google.common.base.Preconditions;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.expediagroup.streamplatform.streamregistry.core.events.handlers.SchemaEventHandlerForKafka;
import com.expediagroup.streamplatform.streamregistry.model.Schema;

@Slf4j
@Configuration
@EnableConfigurationProperties({NotificationEventConfig.NewTopicProperties.class, NotificationEventConfig.SchemaParserProperties.class})
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

  @Data
  @ConfigurationProperties(prefix = KAFKA_TOPIC_SETUP_PROPERTY)
  public static class NewTopicProperties {
    private Integer numPartitions;
    private Short replicationFactor;
    private Map<String, String> configs = null;

    public NewTopic buildNewTopic(final String topicName) {
      // We execute this method and its validations only when 'notification.events.kafka.topic.setup' is true
      // during KafkaSetupHandler building. If we use @Validated and its constraints, bean loading will fail even
      // when topic setup is disabled.
      val component = "enabled Kafka topic setup";

      Objects.requireNonNull(topicName, getWarningMessageOnNotDefinedProp(component, KAFKA_TOPIC_NAME_PROPERTY));
      Objects.requireNonNull(numPartitions, getWarningMessageOnNotDefinedProp(component,
          KAFKA_TOPIC_SETUP_PROPERTY.concat(".numPartitions")));
      Objects.requireNonNull(replicationFactor, getWarningMessageOnNotDefinedProp(component,
          KAFKA_TOPIC_SETUP_PROPERTY.concat(".replicationFactor")));

      val gtZeroWarning = " must be greater than zero";

      Preconditions.checkArgument(numPartitions.compareTo(0) > 0,
          KAFKA_TOPIC_SETUP_PROPERTY.concat(".numPartitions").concat(gtZeroWarning));
      Preconditions.checkArgument(replicationFactor.intValue() > 0,
          KAFKA_TOPIC_SETUP_PROPERTY.concat(".replicationFactor").concat(gtZeroWarning));

      return new NewTopic(topicName, numPartitions, replicationFactor).configs(configs);
    }
  }

  @Data
  @ConfigurationProperties(prefix = CUSTOM_SCHEMA_TYPE_PREFIX)
  public static class SchemaParserProperties {
    private Boolean customEnabled;
    private String keyParserClass;
    private String keyParserMethod;
    private String valueParserClass;
    private String valueParserMethod;

    public Function<Schema, ?> buildSchemaToKeyRecord() {
      return Optional.ofNullable(customEnabled)
          .filter(Boolean::booleanValue)
          .map(e -> this.loadKeyParser())
          .orElse(NotificationEventUtils::toAvroKeyRecord);
    }

    public Function<Schema, ?> buildSchemaToValueRecord() {
      return Optional.ofNullable(customEnabled)
          .filter(Boolean::booleanValue)
          .map(e -> this.loadValueParser())
          .orElse(NotificationEventUtils::toAvroValueRecord);
    }

    private <R extends SpecificRecord> Function<Schema, R> loadKeyParser() {
      Objects.requireNonNull(keyParserClass, getWarningMessageOnNotDefinedProp("enabled schema type parser", CUSTOM_SCHEMA_KEY_PARSER_CLASS_PROPERTY));
      Objects.requireNonNull(keyParserMethod, getWarningMessageOnNotDefinedProp("enabled schema type parser", CUSTOM_SCHEMA_KEY_PARSER_METHOD_PROPERTY));

      try {
        return NotificationEventUtils.loadToAvroStaticMethod(keyParserClass, keyParserMethod, Schema.class);
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    }

    private <R extends SpecificRecord> Function<Schema, R> loadValueParser() {
      Objects.requireNonNull(valueParserClass, getWarningMessageOnNotDefinedProp("enabled schema type parser", CUSTOM_SCHEMA_VALUE_PARSER_CLASS_PROPERTY));
      Objects.requireNonNull(valueParserMethod, getWarningMessageOnNotDefinedProp("enabled schema type parser", CUSTOM_SCHEMA_VALUE_PARSER_METHOD_PROPERTY));

      try {
        return NotificationEventUtils.loadToAvroStaticMethod(valueParserClass, valueParserMethod, Schema.class);
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    }
  }

  public static String getWarningMessageOnNotDefinedProp(String component, String property) {
    return String.format("%s prop must be configured on %s", property, component);
  }
}