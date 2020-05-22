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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.state.internal.EventCorrelator;
import com.expediagroup.streamplatform.streamregistry.state.avro.AvroConverter;
import com.expediagroup.streamplatform.streamregistry.state.avro.AvroEvent;
import com.expediagroup.streamplatform.streamregistry.state.avro.AvroKey;
import com.expediagroup.streamplatform.streamregistry.state.avro.AvroValue;
import com.expediagroup.streamplatform.streamregistry.state.kafka.KafkaEventSender.Config;
import com.expediagroup.streamplatform.streamregistry.state.kafka.KafkaEventSender.CorrelationStrategyImpl;
import com.expediagroup.streamplatform.streamregistry.state.kafka.KafkaEventSender.NullCorrelationStrategy;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;

@RunWith(MockitoJUnitRunner.class)
public class KafkaEventSenderTest {
  @Mock private Config config;
  @Mock private AvroConverter converter;
  @Mock private KafkaProducer<AvroKey, AvroValue> producer;

  private final ObjectMapper mapper = new ObjectMapper();
  private final DomainKey key = new DomainKey("domain");
  private final DefaultSpecification specification = new DefaultSpecification("description", List.of(), "type", mapper.createObjectNode());
  private final Event<DomainKey, DefaultSpecification> event = Event.of(key, specification);

  @Mock private AvroEvent avroEvent;
  @Mock private AvroKey avroKey;
  @Mock private AvroValue avroValue;

  @Captor ArgumentCaptor<ProducerRecord<AvroKey, AvroValue>> recordCaptor;
  @Captor ArgumentCaptor<Callback> callbackCaptor;

  @Before
  public void before() {
    when(converter.toAvro(event)).thenReturn(avroEvent);
    when(avroEvent.getKey()).thenReturn(avroKey);
    when(avroEvent.getValue()).thenReturn(avroValue);
    when(config.getTopic()).thenReturn("topic");
  }

  @Test
  public void nullCorrelatorSuccess() {
    var correlationStrategy = new NullCorrelationStrategy();

    var underTest = new KafkaEventSender(config, correlationStrategy, converter, producer);

    var result = underTest.send(event);

    verify(producer).send(recordCaptor.capture(), callbackCaptor.capture());

    var record = recordCaptor.getValue();
    assertThat(record.topic(), is("topic"));
    assertThat(record.key(), is(avroKey));
    assertThat(record.value(), is(avroValue));
    assertThat(record.headers().toArray().length, is(0));

    var callback = callbackCaptor.getValue();
    assertThat(result.isDone(), is(false));
    var recordMetadata = mock(RecordMetadata.class);
    callback.onCompletion(recordMetadata, null);
    assertThat(result.isDone(), is(true));
  }

  @Test
  public void correlatorSuccess() {
    var correlator = mock(EventCorrelator.class);
    var correlationStrategy = new CorrelationStrategyImpl(correlator);

    var underTest = new KafkaEventSender(config, correlationStrategy, converter, producer);

    when(correlator.register(any())).thenReturn("correlationId");

    var result = underTest.send(event);

    verify(correlator).register(result);
    verify(producer).send(recordCaptor.capture(), callbackCaptor.capture());

    var record = recordCaptor.getValue();
    assertThat(record.topic(), is("topic"));
    assertThat(record.key(), is(avroKey));
    assertThat(record.value(), is(avroValue));
    assertThat(record.headers().toArray().length, is(1));

    var callback = callbackCaptor.getValue();
    assertThat(result.isDone(), is(false));
    var recordMetadata = mock(RecordMetadata.class);
    callback.onCompletion(recordMetadata, null);
    assertThat(result.isDone(), is(false));
  }

  @Test
  public void nullCorrelatorFailure() {
    var correlationStrategy = new NullCorrelationStrategy();

    var underTest = new KafkaEventSender(config, correlationStrategy, converter, producer);

    var result = underTest.send(event);

    verify(producer).send(recordCaptor.capture(), callbackCaptor.capture());

    var callback = callbackCaptor.getValue();
    assertThat(result.isDone(), is(false));
    var e = new Exception();
    callback.onCompletion(null, e);
    assertThat(result.isCompletedExceptionally(), is(true));
  }

  @Test
  public void correlatorFailure() {
    var correlator = mock(EventCorrelator.class);
    var correlationStrategy = new CorrelationStrategyImpl(correlator);

    var underTest = new KafkaEventSender(config, correlationStrategy, converter, producer);

    when(correlator.register(any())).thenReturn("correlationId");

    var result = underTest.send(event);

    verify(correlator).register(any());
    verify(producer).send(recordCaptor.capture(), callbackCaptor.capture());

    var record = recordCaptor.getValue();
    assertThat(record.topic(), is("topic"));
    assertThat(record.key(), is(avroKey));
    assertThat(record.value(), is(avroValue));
    assertThat(record.headers().toArray().length, is(1));

    var callback = callbackCaptor.getValue();
    assertThat(result.isDone(), is(false));
    var e = new Exception();
    callback.onCompletion(null, e);
    assertThat(result.isCompletedExceptionally(), is(false));
    verify(correlator).failed("correlationId", e);
  }
}
