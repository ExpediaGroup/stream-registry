/**
 * Copyright (C) 2018-2024 Expedia, Inc.
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
import static com.expediagroup.streamplatform.streamregistry.state.kafka.KafkaEventReceiver.State.CREATED;
import static com.expediagroup.streamplatform.streamregistry.state.kafka.KafkaEventReceiver.State.ERROR;
import static com.expediagroup.streamplatform.streamregistry.state.kafka.KafkaEventReceiver.State.NOT_RUNNING;
import static com.expediagroup.streamplatform.streamregistry.state.kafka.KafkaEventReceiver.State.PENDING_SHUTDOWN;
import static com.expediagroup.streamplatform.streamregistry.state.kafka.KafkaEventReceiver.State.RUNNING;
import static com.expediagroup.streamplatform.streamregistry.state.model.event.Event.LOAD_COMPLETE;
import static io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PACKAGE;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.*;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import com.expediagroup.streamplatform.streamregistry.state.Configurator;
import com.expediagroup.streamplatform.streamregistry.state.EventReceiver;
import com.expediagroup.streamplatform.streamregistry.state.EventReceiverListener;
import com.expediagroup.streamplatform.streamregistry.state.avro.AvroConverter;
import com.expediagroup.streamplatform.streamregistry.state.avro.AvroKey;
import com.expediagroup.streamplatform.streamregistry.state.avro.AvroValue;
import com.expediagroup.streamplatform.streamregistry.state.internal.EventCorrelator;
import com.expediagroup.streamplatform.streamregistry.state.model.event.StatusDeletionEvent;
import com.expediagroup.streamplatform.streamregistry.state.model.event.StatusEvent;

import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@RequiredArgsConstructor(access = PACKAGE)
public class KafkaEventReceiver implements EventReceiver {

  /**
   * Most of the time, there will only ever be a single process running on the ExecutorService. However, we need this to be two during
   * application bootstrapping. 1 thread for the consumer, 1 thread for the progress logger.
   */
  private static final int THREAD_POOL_SIZE = 2;


  @NonNull
  private final Config config;
  private final EventCorrelator correlator;
  @NonNull
  private final AvroConverter converter;
  @NonNull
  private final KafkaConsumer<AvroKey, AvroValue> consumer;
  @NonNull
  private final ScheduledExecutorService executorService;

  private final AtomicReference<State> state = new AtomicReference<>(CREATED);

  private volatile boolean shuttingDown = false;
  private final AtomicBoolean started = new AtomicBoolean(false);

  public KafkaEventReceiver(Config config, EventCorrelator correlator, Configurator<KafkaConsumer<AvroKey, AvroValue>> consumerConfigurator) {
    this(
      config,
      correlator,
      new AvroConverter(),
      getKafkaConsumer(config, consumerConfigurator),
      newScheduledThreadPool(THREAD_POOL_SIZE)
    );
  }

  public KafkaEventReceiver(Config config, EventCorrelator correlator) {
    this(config, correlator, kafkaConsumer -> {});
  }

  public KafkaEventReceiver(Config config) {
    this(config, null);
  }


  private static KafkaConsumer<AvroKey, AvroValue> getKafkaConsumer(Config config, Configurator<KafkaConsumer<AvroKey, AvroValue>> consumerConfigurator) {
    KafkaConsumer<AvroKey, AvroValue> kafkaConsumer = new KafkaConsumer<>(consumerConfig(config));
    consumerConfigurator.configure(kafkaConsumer);
    return kafkaConsumer;
  }

  @Override
  public void receive(EventReceiverListener listener) {
    if (state.getAndSet(RUNNING) != CREATED) {
      throw new IllegalStateException("Only a single EventReceiverListener is supported");
    }
    executorService.execute(() -> {
      try {
        consume(listener);
      } catch (Exception e) {
        log.error("Receiving failed", e);
        state.set(ERROR);
        throw e;
      }
    });
  }

  void consume(EventReceiverListener listener) {
    val currentOffset = new AtomicLong(0L);
    val progressLogger = executorService
      .scheduleAtFixedRate(() -> log.info("Current offset {}", currentOffset.get()), 10, 10, SECONDS);

    val topicPartition = new TopicPartition(config.getTopic(), 0);
    val topicPartitions = Collections.singletonList(topicPartition);

    int partitions = consumer.partitionsFor(topicPartition.topic()).size();
    if (partitions != 1) {
      throw new IllegalStateException("Unsupported partition count. Require 1, got " + partitions);
    }

    long beginningOffset = consumer.beginningOffsets(topicPartitions).get(topicPartition);
    long endOffset = consumer.endOffsets(topicPartitions).get(topicPartition);
    log.info("Offsets: beginning[{}], end[{}]", beginningOffset, endOffset);

    consumer.assign(topicPartitions);
    consumer.seekToBeginning(topicPartitions);

    boolean loaded = false;
    if (endOffset == 0L) {
      progressLogger.cancel(true);
      log.info("Loading complete. Empty topic.");
      listener.onEvent(LOAD_COMPLETE);
      loaded = true;
    }

    while (state.get() == RUNNING) {
      for (ConsumerRecord<AvroKey, AvroValue> record : consumer.poll(Duration.ofMillis(100))) {
        val event = converter.toModel(record.key(), record.value());
        currentOffset.set(record.offset());
        try {
          if (!config.getEntityStatusEnabled() && (event instanceof StatusEvent || event instanceof StatusDeletionEvent)) {
            log.warn("Entity Status is disabled and will not trigger onEvent key={}", event.getKey());
          } else {
            listener.onEvent(event);
          }
        } catch (Exception e) {
          log.error("Listener failed for event {}", event, e);
        }
        receiveCorrelationId(record);
        if (!loaded && record.offset() >= endOffset - 1L) {
          progressLogger.cancel(true);
          log.info("Loading complete. Reached offset " + record.offset());
          listener.onEvent(LOAD_COMPLETE);
          loaded = true;
        }
      }
    }
  }

  private void receiveCorrelationId(ConsumerRecord<?, ?> record) {
    if (correlator != null) {
      val headerIterator = record.headers().headers(CORRELATION_ID).iterator();
      if (headerIterator.hasNext()) {
        val header = headerIterator.next();
        val correlationId = new String(header.value(), UTF_8);
        correlator.received(correlationId);
      }
    }
  }

  @Override
  public void close() {
    state.set(PENDING_SHUTDOWN);
    executorService.shutdown();
    consumer.close();
    state.set(NOT_RUNNING);
  }

  public State getState() {
    return state.get();
  }

  static Map<String, Object> consumerConfig(Config config) {
    Map<String, Object> kafkaConfigs = new HashMap<>();

    if (config.getProperties() != null) {
      kafkaConfigs.putAll(config.getProperties());
    }

    kafkaConfigs.put(BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
    kafkaConfigs.put(GROUP_ID_CONFIG, config.getGroupId());
    kafkaConfigs.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
    kafkaConfigs.put(ENABLE_AUTO_COMMIT_CONFIG, false);
    kafkaConfigs.put(KEY_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
    kafkaConfigs.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
    kafkaConfigs.put(SCHEMA_REGISTRY_URL_CONFIG, config.getSchemaRegistryUrl());
    kafkaConfigs.put(SPECIFIC_AVRO_READER_CONFIG, true);

    return kafkaConfigs;
  }

  @Value
  @Builder
  public static class Config {
    @NonNull String bootstrapServers;
    @NonNull String topic;
    @NonNull String schemaRegistryUrl;
    @NonNull String groupId;
    Map<String, Object> properties;
    @Builder.Default Boolean entityStatusEnabled = true;
  }

  public enum State {
    CREATED,
    RUNNING,
    ERROR,
    PENDING_SHUTDOWN,
    NOT_RUNNING
  }
}
