/**
 * Copyright (C) 2018-2021 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.core.services;

import java.util.Optional;

import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamBindingKey;
import com.expediagroup.streamplatform.streamregistry.repository.ConsumerRepository;
import com.expediagroup.streamplatform.streamregistry.repository.ProducerRepository;
import com.expediagroup.streamplatform.streamregistry.repository.SchemaRepository;
import com.expediagroup.streamplatform.streamregistry.repository.StreamBindingRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.StreamValidator;
import com.expediagroup.streamplatform.streamregistry.core.views.ConsumerView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProducerView;
import com.expediagroup.streamplatform.streamregistry.core.views.SchemaView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamView;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;
import com.expediagroup.streamplatform.streamregistry.repository.StreamRepository;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StreamServiceTest {

  @Mock
  private HandlerService handlerService;

  @Mock
  private StreamValidator streamValidator;

  @Mock
  private StreamRepository streamRepository;

  @Mock
  private ConsumerService consumerService;

  @Mock
  private ProducerService producerService;

  @Mock
  private SchemaService schemaService;

  @Mock
  private StreamBindingService streamBindingService;

  @Mock
  private StreamBindingRepository streamBindingRepository;

  @Mock
  private ConsumerRepository consumerRepository;

  @Mock
  private ProducerRepository producerRepository;

  @Mock
  private SchemaRepository schemaRepository;

  private StreamService streamService;

  @Before
  public void before() {

    streamService = new StreamService(
      handlerService,
      streamValidator,
      streamRepository,
      streamBindingService,
      producerService,
      consumerService,
      schemaService,
      new StreamView(streamRepository),
      new StreamBindingView(streamBindingRepository),
      new ProducerView(producerRepository),
      new ConsumerView(consumerRepository),
      new SchemaView(schemaRepository)
    );
  }

  @Test
  public void create() {
    final StreamKey key = mock(StreamKey.class);
    final Specification specification = mock(Specification.class);

    final Stream entity = mock(Stream.class);
    when(entity.getKey()).thenReturn(key);
    when(streamRepository.findById(key)).thenReturn(empty());

    doNothing().when(streamValidator).validateForCreate(entity);
    when(handlerService.handleInsert(entity)).thenReturn(specification);

    when(streamRepository.save(entity)).thenReturn(entity);

    streamService.create(entity);

    verify(entity).getKey();
    verify(streamRepository).findById(key);
    verify(streamValidator).validateForCreate(entity);
    verify(handlerService).handleInsert(entity);
    verify(streamRepository).save(entity);
  }

  @Test
  public void update() {
    final StreamKey key = mock(StreamKey.class);
    final Specification specification = mock(Specification.class);

    final Stream entity = mock(Stream.class);
    final Stream existingEntity = mock(Stream.class);

    when(entity.getKey()).thenReturn(key);

    when(streamRepository.findById(key)).thenReturn(Optional.of(existingEntity));
    doNothing().when(streamValidator).validateForUpdate(entity, existingEntity);
    when(handlerService.handleUpdate(entity, existingEntity)).thenReturn(specification);

    when(streamRepository.save(entity)).thenReturn(entity);

    streamService.update(entity);

    verify(entity).getKey();
    verify(streamRepository).findById(key);
    verify(streamValidator).validateForUpdate(entity, existingEntity);
    verify(handlerService).handleUpdate(entity, existingEntity);
    verify(streamRepository).save(entity);
  }

  @Test
  public void updateStatus() {
    final Status status = mock(Status.class);
    final Stream entity = mock(Stream.class);

    when(streamRepository.save(entity)).thenReturn(entity);

    streamService.updateStatus(entity, status);

    verify(streamRepository).save(entity);
  }

  @Test
  public void delete() {
    final SchemaKey schemaKey = mock(SchemaKey.class);
    final Schema schema = mock(Schema.class);

    final StreamKey key = mock(StreamKey.class);
    final Stream entity = mock(Stream.class);
    when(entity.getSchemaKey()).thenReturn(schemaKey);
    when(entity.getKey()).thenReturn(key);

    final StreamBindingKey bindingKey = mock(StreamBindingKey.class);
    final StreamBinding binding = mock(StreamBinding.class);
    when(bindingKey.getStreamKey()).thenReturn(key);
    when(binding.getKey()).thenReturn(bindingKey);

    final ConsumerKey consumerKey = mock(ConsumerKey.class);
    final Consumer consumer = mock(Consumer.class);
    when(consumerKey.getStreamKey()).thenReturn(key);
    when(consumer.getKey()).thenReturn(consumerKey);

    final ProducerKey producerKey = mock(ProducerKey.class);
    final Producer producer = mock(Producer.class);
    when(producerKey.getStreamKey()).thenReturn(key);
    when(producer.getKey()).thenReturn(producerKey);

    when(streamBindingRepository.findAll()).thenReturn(asList(binding));
    when(producerRepository.findAll()).thenReturn(asList(producer));
    when(consumerRepository.findAll()).thenReturn(asList(consumer));
    when(schemaRepository.findById(any())).thenReturn(Optional.of(schema));
    when(streamRepository.findAll()).thenReturn(asList(entity));

    streamService.delete(entity);

    InOrder inOrder = inOrder(schemaService, producerService, consumerService, streamBindingService, streamRepository);
    inOrder.verify(streamBindingService).delete(binding);
    inOrder.verify(consumerService).delete(consumer);
    inOrder.verify(producerService).delete(producer);
    inOrder.verify(streamRepository).delete(entity);
    inOrder.verify(schemaService).delete(schema);
  }

  @Test
  public void delete_preserveSharedSchema() {
    final SchemaKey schemaKey = mock(SchemaKey.class);
    final Schema schema = mock(Schema.class);

    final StreamKey key = mock(StreamKey.class);
    final Stream entity = mock(Stream.class);
    when(entity.getSchemaKey()).thenReturn(schemaKey);
    when(entity.getKey()).thenReturn(key);

    final StreamKey otherKey = mock(StreamKey.class);
    final Stream otherStream = mock(Stream.class);
    when(otherStream.getSchemaKey()).thenReturn(schemaKey);
    when(otherStream.getKey()).thenReturn(otherKey);

    final StreamBindingKey bindingKey = mock(StreamBindingKey.class);
    final StreamBinding binding = mock(StreamBinding.class);
    when(bindingKey.getStreamKey()).thenReturn(key);
    when(binding.getKey()).thenReturn(bindingKey);

    final ConsumerKey consumerKey = mock(ConsumerKey.class);
    final Consumer consumer = mock(Consumer.class);
    when(consumerKey.getStreamKey()).thenReturn(key);
    when(consumer.getKey()).thenReturn(consumerKey);

    final ProducerKey producerKey = mock(ProducerKey.class);
    final Producer producer = mock(Producer.class);
    when(producerKey.getStreamKey()).thenReturn(key);
    when(producer.getKey()).thenReturn(producerKey);



    when(streamBindingRepository.findAll()).thenReturn(asList(binding));
    when(producerRepository.findAll()).thenReturn(asList(producer));
    when(consumerRepository.findAll()).thenReturn(asList(consumer));
    when(streamRepository.findAll()).thenReturn(asList(entity, otherStream));

    streamService.delete(entity);

    InOrder inOrder = inOrder(schemaService, producerService, consumerService, streamBindingService, streamRepository);
    inOrder.verify(streamBindingService).delete(binding);
    inOrder.verify(consumerService).delete(consumer);
    inOrder.verify(producerService).delete(producer);
    inOrder.verify(streamRepository).delete(entity);
    inOrder.verify(schemaService, never()).delete(schema);
  }

  @Test
  public void delete_noChildren() {
    final Stream entity = mock(Stream.class);

    when(streamBindingRepository.findAll()).thenReturn(emptyList());
    when(producerRepository.findAll()).thenReturn(emptyList());
    when(consumerRepository.findAll()).thenReturn(emptyList());
    when(schemaRepository.findById(any())).thenReturn(empty());
    when(streamRepository.findAll()).thenReturn(emptyList());

    streamService.delete(entity);

    InOrder inOrder = inOrder(schemaService, producerService, consumerService, streamBindingService, streamRepository);
    inOrder.verify(streamBindingService, never()).delete(any());
    inOrder.verify(consumerService, never()).delete(any());
    inOrder.verify(producerService, never()).delete(any());
    inOrder.verify(streamRepository).delete(entity);
    inOrder.verify(schemaService, never()).delete(any());
  }


  @Test
  public void delete_multi() {
    final SchemaKey schemaKey = mock(SchemaKey.class);
    final Schema schema = mock(Schema.class);

    final StreamKey key = mock(StreamKey.class);
    final Stream entity = mock(Stream.class);
    when(entity.getSchemaKey()).thenReturn(schemaKey);
    when(entity.getKey()).thenReturn(key);

    final StreamBindingKey bindingKey1 = mock(StreamBindingKey.class);
    final StreamBinding binding1 = mock(StreamBinding.class);
    when(bindingKey1.getStreamKey()).thenReturn(key);
    when(binding1.getKey()).thenReturn(bindingKey1);

    final StreamBindingKey bindingKey2 = mock(StreamBindingKey.class);
    final StreamBinding binding2 = mock(StreamBinding.class);
    when(bindingKey2.getStreamKey()).thenReturn(key);
    when(binding2.getKey()).thenReturn(bindingKey2);

    final ConsumerKey consumerKey1 = mock(ConsumerKey.class);
    final Consumer consumer1 = mock(Consumer.class);
    when(consumerKey1.getStreamKey()).thenReturn(key);
    when(consumer1.getKey()).thenReturn(consumerKey1);

    final ConsumerKey consumerKey2 = mock(ConsumerKey.class);
    final Consumer consumer2 = mock(Consumer.class);
    when(consumerKey2.getStreamKey()).thenReturn(key);
    when(consumer2.getKey()).thenReturn(consumerKey2);

    final ProducerKey producerKey1 = mock(ProducerKey.class);
    final Producer producer1 = mock(Producer.class);
    when(producerKey1.getStreamKey()).thenReturn(key);
    when(producer1.getKey()).thenReturn(producerKey1);

    final ProducerKey producerKey2 = mock(ProducerKey.class);
    final Producer producer2 = mock(Producer.class);
    when(producerKey2.getStreamKey()).thenReturn(key);
    when(producer2.getKey()).thenReturn(producerKey2);

    when(streamBindingRepository.findAll()).thenReturn(asList(binding1, binding2));
    when(producerRepository.findAll()).thenReturn(asList(producer1, producer2));
    when(consumerRepository.findAll()).thenReturn(asList(consumer1, consumer2));
    when(schemaRepository.findById(any())).thenReturn(Optional.of(schema));
    when(streamRepository.findAll()).thenReturn(asList(entity));

    streamService.delete(entity);

    InOrder inOrder = inOrder(schemaService, producerService, consumerService, streamBindingService, streamRepository);
    inOrder.verify(streamBindingService).delete(binding1);
    inOrder.verify(streamBindingService).delete(binding2);
    inOrder.verify(consumerService).delete(consumer1);
    inOrder.verify(consumerService).delete(consumer2);
    inOrder.verify(producerService).delete(producer1);
    inOrder.verify(producerService).delete(producer2);
    inOrder.verify(streamRepository).delete(entity);
    inOrder.verify(schemaService).delete(schema);
  }

}
