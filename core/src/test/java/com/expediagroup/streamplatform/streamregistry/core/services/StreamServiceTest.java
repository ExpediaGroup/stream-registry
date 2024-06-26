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
package com.expediagroup.streamplatform.streamregistry.core.services;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.StreamValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.core.views.ConsumerView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProcessView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProducerView;
import com.expediagroup.streamplatform.streamregistry.core.views.SchemaView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamView;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.Process;
import com.expediagroup.streamplatform.streamregistry.model.ProcessInputStream;
import com.expediagroup.streamplatform.streamregistry.model.ProcessOutputStream;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProcessKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;
import com.expediagroup.streamplatform.streamregistry.repository.ConsumerRepository;
import com.expediagroup.streamplatform.streamregistry.repository.ProcessRepository;
import com.expediagroup.streamplatform.streamregistry.repository.ProducerRepository;
import com.expediagroup.streamplatform.streamregistry.repository.SchemaRepository;
import com.expediagroup.streamplatform.streamregistry.repository.StreamBindingRepository;
import com.expediagroup.streamplatform.streamregistry.repository.StreamRepository;

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
  private ProcessService processService;

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

  @Mock
  private ProcessRepository processRepository;

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
      processService,
      new StreamView(streamRepository),
      new StreamBindingView(streamBindingRepository),
      new ProducerView(producerRepository),
      new ConsumerView(consumerRepository),
      new SchemaView(schemaRepository),
      new ProcessView(processRepository)
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

    when(streamRepository.saveSpecification(entity)).thenReturn(entity);

    streamService.create(entity);

    verify(entity).getKey();
    verify(streamRepository).findById(key);
    verify(streamValidator).validateForCreate(entity);
    verify(handlerService).handleInsert(entity);
    verify(streamRepository).saveSpecification(entity);
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

    when(streamRepository.saveSpecification(entity)).thenReturn(entity);

    streamService.update(entity);

    verify(entity).getKey();
    verify(streamRepository).findById(key);
    verify(streamValidator).validateForUpdate(entity, existingEntity);
    verify(handlerService).handleUpdate(entity, existingEntity);
    verify(streamRepository).saveSpecification(entity);
  }

  @Test
  public void updateWithChangedSchemaKey() {
    StreamKey key = new StreamKey();
    key.setDomain("domain");
    key.setName("stream");
    key.setVersion(1);
    SchemaKey existingSchema = new SchemaKey("domain", "existing");
    SchemaKey updatedSchema = new SchemaKey("domain", "updated");
    Stream existingEntity = new Stream(key, new Specification(), existingSchema);
    Stream updatedEntity = new Stream(key, new Specification(), updatedSchema);
    when(streamRepository.findById(key)).thenReturn(Optional.of(existingEntity));
    ValidationException ex = assertThrows(ValidationException.class, () -> streamService.update(updatedEntity));
    assertEquals("Stream = " + key + " update failed, because existing schemaKey = " + existingSchema +
      " is not matching with given schemaKey = " + updatedSchema, ex.getMessage());
    verify(streamRepository).findById(key);
  }

  @Test
  public void updateWithSchemaKeyNull() {
    StreamKey key = new StreamKey();
    key.setDomain("domain");
    key.setName("stream");
    key.setVersion(1);
    SchemaKey schemaKey = new SchemaKey("domain", "stream_v1");
    Stream existingEntity = new Stream(key, new Specification(), schemaKey);
    Stream updatedEntity = new Stream(key, new Specification(), null);
    when(streamRepository.findById(key)).thenReturn(Optional.of(existingEntity));
    streamService.update(updatedEntity);
    // should update the schemaKey if null in update
    assertNotNull(updatedEntity.getSchemaKey());
    assertEquals(updatedEntity.getSchemaKey(), schemaKey);

    verify(streamRepository).findById(key);
    verify(streamValidator).validateForUpdate(updatedEntity, existingEntity);
    verify(handlerService).handleUpdate(updatedEntity, existingEntity);
    verify(streamRepository).saveSpecification(updatedEntity);
  }

  @Test
  public void updateStatus() {
    final Status status = mock(Status.class);
    final Stream entity = mock(Stream.class);

    when(streamRepository.saveStatus(entity)).thenReturn(entity);

    streamService.updateStatus(entity, status);

    verify(streamRepository).saveStatus(entity);
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

    when(processRepository.findAll()).thenReturn(emptyList());
    when(streamBindingRepository.findAll()).thenReturn(List.of(binding));
    when(producerRepository.findAll()).thenReturn(List.of(producer));
    when(consumerRepository.findAll()).thenReturn(List.of(consumer));
    when(schemaRepository.findById(any())).thenReturn(Optional.of(schema));
    when(streamRepository.findAll()).thenReturn(List.of(entity));

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

    when(processRepository.findAll()).thenReturn(emptyList());
    when(streamBindingRepository.findAll()).thenReturn(List.of(binding));
    when(producerRepository.findAll()).thenReturn(List.of(producer));
    when(consumerRepository.findAll()).thenReturn(List.of(consumer));
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

    when(processRepository.findAll()).thenReturn(emptyList());
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

    when(processRepository.findAll()).thenReturn(emptyList());
    when(streamBindingRepository.findAll()).thenReturn(asList(binding1, binding2));
    when(producerRepository.findAll()).thenReturn(asList(producer1, producer2));
    when(consumerRepository.findAll()).thenReturn(asList(consumer1, consumer2));
    when(schemaRepository.findById(any())).thenReturn(Optional.of(schema));
    when(streamRepository.findAll()).thenReturn(List.of(entity));

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

  @Test
  public void delete_singleStreamProcesses() {
    final StreamKey streamKey = mock(StreamKey.class);
    final Stream stream = mock(Stream.class);
    when(stream.getKey()).thenReturn(streamKey);

    final StreamKey otherStreamKey = mock(StreamKey.class);

    final Process otherProcess = mock(Process.class);
    when(otherProcess.getInputs()).thenReturn(List.of(new ProcessInputStream(otherStreamKey, new ObjectMapper().createObjectNode())));
    when(otherProcess.getOutputs()).thenReturn(emptyList());

    final Process inputProcess = mock(Process.class);
    when(inputProcess.getInputs()).thenReturn(List.of(new ProcessInputStream(streamKey, new ObjectMapper().createObjectNode())));
    when(inputProcess.getOutputs()).thenReturn(emptyList());

    final Process outputProcess = mock(Process.class);
    when(outputProcess.getInputs()).thenReturn(emptyList());
    when(outputProcess.getOutputs()).thenReturn(List.of(new ProcessOutputStream(streamKey, new ObjectMapper().createObjectNode())));

    final Process inputOutputProcess = mock(Process.class);
    when(inputOutputProcess.getInputs()).thenReturn(List.of(new ProcessInputStream(streamKey, new ObjectMapper().createObjectNode())));
    when(inputOutputProcess.getOutputs()).thenReturn(List.of(new ProcessOutputStream(streamKey, new ObjectMapper().createObjectNode())));

    when(processRepository.findAll()).thenReturn(asList(otherProcess, inputProcess, outputProcess, inputOutputProcess));
    when(streamBindingRepository.findAll()).thenReturn(emptyList());
    when(producerRepository.findAll()).thenReturn(emptyList());
    when(consumerRepository.findAll()).thenReturn(emptyList());
    when(streamRepository.findAll()).thenReturn(emptyList());

    streamService.delete(stream);

    verify(processService, never()).delete(otherProcess);
    verify(processService).delete(inputProcess);
    verify(processService).delete(outputProcess);
    verify(processService).delete(inputOutputProcess);
  }

  @Test(expected = IllegalStateException.class)
  public void delete_failsOnMultipleStreamProcess() {
    final StreamKey streamKey = mock(StreamKey.class);
    final Stream stream = mock(Stream.class);
    when(stream.getKey()).thenReturn(streamKey);

    final StreamKey otherStreamKey = mock(StreamKey.class);

    final ProcessKey processKey = mock(ProcessKey.class);
    final Process process = mock(Process.class);
    when(process.getInputs()).thenReturn(List.of(new ProcessInputStream(otherStreamKey, new ObjectMapper().createObjectNode())));
    when(process.getOutputs()).thenReturn(List.of(new ProcessOutputStream(streamKey, new ObjectMapper().createObjectNode())));
    when(process.getKey()).thenReturn(processKey);
    when(processRepository.findAll()).thenReturn(List.of(process));

    streamService.delete(stream);
  }
}
