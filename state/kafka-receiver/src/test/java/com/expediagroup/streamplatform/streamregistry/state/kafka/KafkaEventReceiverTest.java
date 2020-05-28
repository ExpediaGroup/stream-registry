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
package com.expediagroup.streamplatform.streamregistry.state.kafka;

import static com.expediagroup.streamplatform.streamregistry.state.internal.EventCorrelator.CORRELATION_ID;
import static com.expediagroup.streamplatform.streamregistry.state.model.event.Event.LOAD_COMPLETE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
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
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;

@RunWith(MockitoJUnitRunner.Silent.class)
public class KafkaEventReceiverTest {
  @Mock private KafkaEventReceiver.Config config;
  @Mock private EventCorrelator correlator;
  @Mock private AvroConverter converter;
  @Mock private KafkaConsumer<AvroKey, AvroValue> consumer;
  @Mock private EventReceiverListener listener;
  @Mock private PartitionInfo partitionInfo;
  @Mock private ConsumerRecord<AvroKey, AvroValue> record;
  @Mock private AvroKey avroKey;
  @Mock private AvroValue avroValue;
  @Mock private Event event;

  private final ScheduledExecutorService executorService = newScheduledThreadPool(2);

  private KafkaEventReceiver underTest;

  private final String topic = "topic";
  private final TopicPartition topicPartition = new TopicPartition(topic, 0);
  private final List<TopicPartition> topicPartitions = List.of(topicPartition);

  @Before
  public void before() {
    underTest = new KafkaEventReceiver(config, correlator, converter, consumer, executorService);
  }

  @Test
  public void typical() throws Exception {
    when(config.getTopic()).thenReturn(topic);
    when(consumer.partitionsFor(topic)).thenReturn(List.of(partitionInfo));
    when(consumer.beginningOffsets(topicPartitions)).thenReturn(Map.of(topicPartition, 0L));
    when(consumer.endOffsets(topicPartitions)).thenReturn(Map.of(topicPartition, 0L));
    when(consumer.poll(Duration.ofMillis(100))).thenReturn(new ConsumerRecords<>(Map.of(topicPartition, List.of(record))));
    when(record.key()).thenReturn(avroKey);
    when(record.value()).thenReturn(avroValue);
    when(converter.toModel(avroKey, avroValue)).thenReturn(event);
    when(record.headers()).thenReturn(new RecordHeaders(List.of(new RecordHeader(CORRELATION_ID, "foo".getBytes(UTF_8)))));

    underTest.receive(listener);
    Thread.sleep(100L);
    underTest.close();

    var inOrder = Mockito.inOrder(consumer, listener, correlator);
    inOrder.verify(consumer).assign(topicPartitions);
    inOrder.verify(consumer).seekToBeginning(topicPartitions);
    inOrder.verify(listener).onEvent(LOAD_COMPLETE);
    inOrder.verify(listener).onEvent(event);
    inOrder.verify(correlator).received("foo");
  }

  @Test(expected = IllegalStateException.class)
  public void incorrectNumberOfPartitions() {
    when(consumer.partitionsFor(topic)).thenReturn(List.of());

    underTest.consume(listener);
  }
}
