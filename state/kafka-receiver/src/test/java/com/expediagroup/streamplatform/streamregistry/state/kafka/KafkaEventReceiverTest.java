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
import static com.expediagroup.streamplatform.streamregistry.state.kafka.KafkaEventReceiver.State.*;
import static com.expediagroup.streamplatform.streamregistry.state.model.event.Event.LOAD_COMPLETE;
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.*;

import lombok.val;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.awaitility.*;
import org.awaitility.core.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.state.EventReceiverListener;
import com.expediagroup.streamplatform.streamregistry.state.avro.AvroConverter;
import com.expediagroup.streamplatform.streamregistry.state.avro.AvroKey;
import com.expediagroup.streamplatform.streamregistry.state.avro.AvroValue;
import com.expediagroup.streamplatform.streamregistry.state.internal.EventCorrelator;
import com.expediagroup.streamplatform.streamregistry.state.kafka.KafkaEventReceiver.Config;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.event.*;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;

@RunWith(MockitoJUnitRunner.Silent.class)
public class KafkaEventReceiverTest {
  @Mock
  private KafkaEventReceiver.Config config;
  @Mock
  private EventCorrelator correlator;
  @Mock
  private AvroConverter converter;
  @Mock
  private KafkaConsumer<AvroKey, AvroValue> consumer;
  @Mock
  private EventReceiverListener listener;
  @Mock
  private PartitionInfo partitionInfo;
  @Mock
  private ConsumerRecord<AvroKey, AvroValue> record;
  @Mock
  private AvroKey avroKey;
  @Mock
  private AvroValue avroValue;
  @Mock
  private SpecificationEvent event;
  @Mock
  private SpecificationDeletionEvent deletionEvent;
  @Mock
  private StatusEvent statusEvent;
  @Mock
  private StatusDeletionEvent statusDeletionEvent;

  private final ScheduledExecutorService executorService = newScheduledThreadPool(2);

  private KafkaEventReceiver underTest;

  private final String topic = "topic";
  private final TopicPartition topicPartition = new TopicPartition(topic, 0);
  private final List<TopicPartition> topicPartitions = Collections.singletonList(topicPartition);
  private final ConditionFactory await = Awaitility.await().atMost(2, SECONDS);
  private CountDownLatch latch = new CountDownLatch(1);

  @Before
  public void before() {
    when(config.getTopic()).thenReturn(topic);
    when(config.getEntityStatusEnabled()).thenReturn(true);
    when(consumer.partitionsFor(topic)).thenReturn(Collections.singletonList(partitionInfo));
    when(consumer.beginningOffsets(topicPartitions)).thenReturn(Collections.singletonMap(topicPartition, 0L));
    when(consumer.endOffsets(topicPartitions)).thenReturn(Collections.singletonMap(topicPartition, 0L));
    when(consumer.poll(Duration.ofMillis(100))).thenReturn(new ConsumerRecords<>(Collections.singletonMap(topicPartition, Collections.singletonList(record))));
    when(record.key()).thenReturn(avroKey);
    when(record.value()).thenReturn(avroValue);
    when(converter.toModel(avroKey, avroValue)).thenReturn(event);
    when(record.headers()).thenReturn(new RecordHeaders(Collections.singletonList(new RecordHeader(CORRELATION_ID, "foo".getBytes(UTF_8)))));

    latch = new CountDownLatch(1);
    doAnswer((correlationId) -> {
      latch.countDown();
      return null;
    }).when(correlator).received(anyString());

    underTest = new KafkaEventReceiver(config, correlator, converter, consumer, executorService);
  }

  @Test
  public void typical() throws Exception {
    underTest.receive(listener);
    assertThat(underTest.getState(), is(RUNNING));
    latch.await(1, SECONDS);
    underTest.close();
    assertThat(underTest.getState(), is(NOT_RUNNING));

    val inOrder = Mockito.inOrder(consumer, listener, correlator);
    inOrder.verify(consumer).assign(topicPartitions);
    inOrder.verify(consumer).seekToBeginning(topicPartitions);
    inOrder.verify(listener).onEvent(LOAD_COMPLETE);
    inOrder.verify(listener).onEvent(event);
    inOrder.verify(correlator).received("foo");
  }

  @Test
  public void receiverDoesNotCallOnEventForStatusEventWhenEventStatusDisabled() throws Exception {
    when(config.getEntityStatusEnabled()).thenReturn(false);
    when(converter.toModel(avroKey, avroValue)).thenReturn(statusEvent);

    underTest.receive(listener);
    assertThat(underTest.getState(), is(RUNNING));
    latch.await(1, SECONDS);
    underTest.close();
    assertThat(underTest.getState(), is(NOT_RUNNING));

    val inOrder = Mockito.inOrder(consumer, listener, correlator);
    inOrder.verify(consumer).assign(topicPartitions);
    inOrder.verify(consumer).seekToBeginning(topicPartitions);
    inOrder.verify(listener).onEvent(LOAD_COMPLETE);
    inOrder.verify(listener, never()).onEvent(statusEvent);
    inOrder.verify(correlator).received("foo");
  }

  @Test
  public void receiverDoesNotCallOnEventForStatusDeletionEventWhenEventStatusDisabled() throws Exception {
    when(config.getEntityStatusEnabled()).thenReturn(false);
    when(converter.toModel(avroKey, avroValue)).thenReturn(statusDeletionEvent);

    underTest.receive(listener);
    assertThat(underTest.getState(), is(RUNNING));
    latch.await(1, SECONDS);
    underTest.close();
    assertThat(underTest.getState(), is(NOT_RUNNING));

    val inOrder = Mockito.inOrder(consumer, listener, correlator);
    inOrder.verify(consumer).assign(topicPartitions);
    inOrder.verify(consumer).seekToBeginning(topicPartitions);
    inOrder.verify(listener).onEvent(LOAD_COMPLETE);
    inOrder.verify(listener, never()).onEvent(statusDeletionEvent);
    inOrder.verify(correlator).received("foo");
  }

  @Test
  public void receiverCallsOnEventForSpecificationEventWhenEventStatusDisabled() throws Exception {
    when(config.getEntityStatusEnabled()).thenReturn(false);
    when(converter.toModel(avroKey, avroValue)).thenReturn(event);

    underTest.receive(listener);
    assertThat(underTest.getState(), is(RUNNING));
    latch.await(1, SECONDS);
    underTest.close();
    assertThat(underTest.getState(), is(NOT_RUNNING));

    val inOrder = Mockito.inOrder(consumer, listener, correlator);
    inOrder.verify(consumer).assign(topicPartitions);
    inOrder.verify(consumer).seekToBeginning(topicPartitions);
    inOrder.verify(listener).onEvent(LOAD_COMPLETE);
    inOrder.verify(listener).onEvent(event);
    inOrder.verify(correlator).received("foo");
  }

  @Test
  public void receiverCallsOnEventForDeletionEventWhenEventStatusDisabled() throws Exception {
    when(config.getEntityStatusEnabled()).thenReturn(false);
    when(converter.toModel(avroKey, avroValue)).thenReturn(deletionEvent);

    underTest.receive(listener);
    assertThat(underTest.getState(), is(RUNNING));
    latch.await(1, SECONDS);
    underTest.close();
    assertThat(underTest.getState(), is(NOT_RUNNING));

    val inOrder = Mockito.inOrder(consumer, listener, correlator);
    inOrder.verify(consumer).assign(topicPartitions);
    inOrder.verify(consumer).seekToBeginning(topicPartitions);
    inOrder.verify(listener).onEvent(LOAD_COMPLETE);
    inOrder.verify(listener).onEvent(deletionEvent);
    inOrder.verify(correlator).received("foo");
  }

  @Test
  public void listenerThrowsException() throws Exception {
    doThrow(new RuntimeException("listener error")).when(listener).onEvent(event);

    underTest.receive(listener);
    latch.await(1, SECONDS);
    assertThat(underTest.getState(), is(RUNNING));
    underTest.close();
    assertThat(underTest.getState(), is(NOT_RUNNING));

    val inOrder = Mockito.inOrder(consumer, listener, correlator);
    inOrder.verify(consumer).assign(topicPartitions);
    inOrder.verify(consumer).seekToBeginning(topicPartitions);
    inOrder.verify(listener).onEvent(LOAD_COMPLETE);
    inOrder.verify(listener).onEvent(event);
    inOrder.verify(correlator).received("foo");
  }

  @Test
  public void errorWhenMoreThanOnePartition() {
    val multiplePartitions = new ArrayList<PartitionInfo>() {{
      add(partitionInfo);
      add(partitionInfo);
    }};
    when(consumer.partitionsFor(topic)).thenReturn(multiplePartitions);

    underTest.receive(listener);
    verify(consumer, timeout(100)).partitionsFor(topic);
    await.untilAsserted(() -> assertThat(underTest.getState(), is(ERROR)));

    underTest.close();
    assertThat(underTest.getState(), is(NOT_RUNNING));
  }

  @Test
  public void errorWhenRunningButUnableToPoll() throws Exception {
    val polls = new AtomicInteger(0);
    when(consumer.poll(Duration.ofMillis(100))).thenAnswer(invocation -> {
      if (polls.getAndIncrement() < 10) {
        return new ConsumerRecords<>(Collections.singletonMap(topicPartition, Collections.singletonList(record)));
      } else {
        throw new RuntimeException("Some Kafka poll error here");
      }
    });

    underTest.receive(listener);
    latch.await(1, SECONDS);
    assertThat(underTest.getState(), is(RUNNING));

    val inOrder = Mockito.inOrder(consumer, listener, correlator);
    inOrder.verify(consumer).assign(topicPartitions);
    inOrder.verify(consumer).seekToBeginning(topicPartitions);
    inOrder.verify(listener).onEvent(LOAD_COMPLETE);
    inOrder.verify(listener).onEvent(event);

    await.untilAsserted(() -> assertThat(underTest.getState(), is(ERROR)));
    verify(consumer, times(11)).poll(any());
    underTest.close();
    assertThat(underTest.getState(), is(NOT_RUNNING));
  }

  @Test(expected = IllegalStateException.class)
  public void incorrectNumberOfPartitions() {
    when(consumer.partitionsFor(topic)).thenReturn(Collections.emptyList());

    underTest.consume(listener);
  }

  @Test(expected = IllegalStateException.class)
  public void onlySupportsOneReceiver() {
    val doNothingListener = new EventReceiverListener() {
      @Override
      public <K extends Entity.Key<S>, S extends Specification> void onEvent(Event<K, S> event) {
      }
    };
    underTest.receive(doNothingListener);
    underTest.receive(doNothingListener);
  }

  @Test
  public void propertiesToConfigMapping() {
    Map<String, Object> properties = new HashMap<String, Object>() {{
      put("ssl.keystore.location", "/path/to/cert.jks");
      put("security.protocol", "SSL");
      put("ssl.truststore.location", "/path/to/cert.jks");
      put("ssl.keystore.password", "password");
      put("ssl.key.password", "password");
      put("ssl.truststore.password", "password");
      put("ssl.endpoint.identification.algorithm", "");
    }};
    Config config = new Config("bootstrap", "topic", "schemaRegistry", "groupId", properties, true);

    Map<String, Object> expected = new HashMap<String, Object>() {{
      put(BOOTSTRAP_SERVERS_CONFIG, "bootstrap");
      put(GROUP_ID_CONFIG, "groupId");
      put(AUTO_OFFSET_RESET_CONFIG, "earliest");
      put(ENABLE_AUTO_COMMIT_CONFIG, false);
      put(KEY_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
      put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
      put(SCHEMA_REGISTRY_URL_CONFIG, "schemaRegistry");
      put(SPECIFIC_AVRO_READER_CONFIG, true);
    }};
    expected.putAll(properties);

    assertThat(
      KafkaEventReceiver.consumerConfig(config).entrySet(),
      containsInAnyOrder(expected.entrySet().toArray(new Map.Entry[0]))
    );
  }
}
