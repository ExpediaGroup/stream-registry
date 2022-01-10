/**
 * Copyright (C) 2018-2022 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.state.kafka;

import static com.expediagroup.streamplatform.streamregistry.state.internal.EventCorrelator.CORRELATION_ID;
import static io.confluent.kafka.serializers.KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PACKAGE;
import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import io.confluent.kafka.serializers.KafkaAvroSerializer;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;

import com.expediagroup.streamplatform.streamregistry.state.EventSender;
import com.expediagroup.streamplatform.streamregistry.state.avro.AvroConverter;
import com.expediagroup.streamplatform.streamregistry.state.avro.AvroKey;
import com.expediagroup.streamplatform.streamregistry.state.avro.AvroValue;
import com.expediagroup.streamplatform.streamregistry.state.internal.EventCorrelator;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;

@Slf4j
@RequiredArgsConstructor(access = PACKAGE)
public class KafkaEventSender implements EventSender {
  @NonNull private final Config config;
  @NonNull private final CorrelationStrategy correlationStrategy;
  @NonNull private final AvroConverter converter;
  @NonNull private final KafkaProducer<AvroKey, AvroValue> producer;

  public KafkaEventSender(Config config, EventCorrelator correlator) {
    this(
        config,
        correlator == null ? new NullCorrelationStrategy() : new CorrelationStrategyImpl(correlator),
        new AvroConverter(),
        new KafkaProducer<>(producerConfig(config))
    );
  }

  public KafkaEventSender(Config config) {
    this(config, null);
  }

  @Override
  public <K extends Entity.Key<S>, S extends Specification> CompletableFuture<Void> send(@NonNull Event<K, S> event) {
    val avroEvent = converter.toAvro(event);
    return send(avroEvent.getKey(), avroEvent.getValue());
  }

  private CompletableFuture<Void> send(AvroKey key, AvroValue value) {
    val future = new CompletableFuture<Void>();
    val correlationId = correlationStrategy.correlationId(future);
    val headers = correlationStrategy.headers(correlationId);
    val record = new ProducerRecord<>(config.getTopic(), null, null, key, value, headers);
    log.debug("Sending {} - {}", correlationId, record);
    producer.send(record, correlationStrategy.callback(correlationId, future));
    return future;
  }

  interface CorrelationStrategy {
    String correlationId(CompletableFuture<Void> future);

    List<Header> headers(String correlationId);

    Callback callback(String correlationId, CompletableFuture<Void> future);
  }

  static class NullCorrelationStrategy implements CorrelationStrategy {
    @Override
    public String correlationId(CompletableFuture<Void> future) {
      return null;
    }

    @Override
    public List<Header> headers(String correlationId) {
      return Collections.emptyList();
    }

    @Override
    public Callback callback(String correlationId, CompletableFuture<Void> future) {
      return (rm, e) -> {
        if (rm != null) {
          log.debug("Sent {} - {}", correlationId, rm);
          future.complete(null);
        } else {
          log.error("Error sending record {}", correlationId, e);
          future.completeExceptionally(e);
        }
      };
    }
  }

  @RequiredArgsConstructor
  static class CorrelationStrategyImpl implements CorrelationStrategy {
    private final EventCorrelator correlator;

    @Override
    public String correlationId(CompletableFuture<Void> future) {
      return correlator.register(future);
    }

    @Override
    public List<Header> headers(String correlationId) {
      return Collections.singletonList(new RecordHeader(CORRELATION_ID, correlationId.getBytes(UTF_8)));
    }

    @Override
    public Callback callback(String correlationId, CompletableFuture<Void> future) {
      return (rm, e) -> {
        if (rm != null) {
          log.debug("Sent {} - {}", correlationId, rm);
        } else {
          log.error("Error sending record {}", correlationId, e);
          correlator.failed(correlationId, e);
        }
      };
    }
  }


  @Override
  public void close() {
    producer.close();
  }

  static Map<String, Object> producerConfig(Config config) {
    Map<String, Object> kafkaConfigs = new HashMap<>();

    if (config.getPropertiesPath() != null && !config.getPropertiesPath().isEmpty()) {
      Properties properties = new Properties();

      try {
        File propertiesFile = new File(config.getPropertiesPath());
        properties.load(new FileReader(propertiesFile));
      } catch (FileNotFoundException e) {
        throw new IllegalArgumentException("Could not find properties file: [" + config.getPropertiesPath() + "].");
      } catch (IOException e) {
        throw new IllegalArgumentException("Could not read properties file: [" + config.getPropertiesPath() + "].");
      }

      for (Map.Entry<Object, Object> property: properties.entrySet()) {
        kafkaConfigs.put(property.getKey().toString(), property.getValue());
      }
    }

    kafkaConfigs.put(BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
    kafkaConfigs.put(ACKS_CONFIG, "all");
    kafkaConfigs.put(KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
    kafkaConfigs.put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
    kafkaConfigs.put(SCHEMA_REGISTRY_URL_CONFIG, config.getSchemaRegistryUrl());

    return kafkaConfigs;
  }

  @Value
  @Builder
  public static class Config {
    @NonNull String bootstrapServers;
    @NonNull String topic;
    @NonNull String schemaRegistryUrl;
    String propertiesPath;
  }
}
