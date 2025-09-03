/**
 * Copyright (C) 2018-2025 Expedia, Inc.
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.access.AccessDeniedException;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.ProcessValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.core.views.ProcessBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProcessView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamView;
import com.expediagroup.streamplatform.streamregistry.model.*;
import com.expediagroup.streamplatform.streamregistry.model.Process;
import com.expediagroup.streamplatform.streamregistry.model.keys.*;
import com.expediagroup.streamplatform.streamregistry.repository.ProcessBindingRepository;
import com.expediagroup.streamplatform.streamregistry.repository.ProcessRepository;

@RunWith(MockitoJUnitRunner.class)
public class ProcessServiceTest {

  @Mock
  private HandlerService handlerService;

  @Mock
  private ProcessValidator processValidator;

  @Mock
  private ProcessRepository processRepository;

  @Mock
  private ProcessBindingService processBindingService;

  @Mock
  private ProducerService producerService;

  @Mock
  private ConsumerService consumerService;

  @Mock
  private ProcessBindingRepository processBindingRepository;

  private ProcessService processService;

  @Mock
  private StreamView streamView;

  ZoneKey zoneKey = new ZoneKey("aws_us_east_1");
  Stream inputStream = createTestStream("input");
  Stream outputStream = createTestStream("output");
  Process entity = createTestProcess(inputStream.getKey(), outputStream.getKey());

  Stream newInputStream = createTestStream("new_input");
  Stream newOutputStream = createTestStream("new_output");
  Process newEntity = createTestProcess(newInputStream.getKey(), newOutputStream.getKey());

  @Before
  public void before() {
    processService = new ProcessService(
      handlerService,
      processValidator,
      processRepository,
      processBindingService,
      new ProcessBindingView(processBindingRepository),
      new ProcessView(processRepository),
      consumerService,
      producerService,
      streamView
      );

    when(processRepository.findById(entity.getKey())).thenReturn(empty());
    doNothing().when(processValidator).validateForCreate(entity);
    when(handlerService.handleInsert(entity)).thenReturn(entity.getSpecification());
    when(processRepository.saveSpecification(entity)).thenReturn(entity);
    when(streamView.get(inputStream.getKey())).thenReturn(Optional.of(inputStream));
    when(streamView.get(outputStream.getKey())).thenReturn(Optional.of(outputStream));

    doNothing().when(processValidator).validateForUpdate(newEntity, entity);
    when(handlerService.handleUpdate(newEntity, entity)).thenReturn(newEntity.getSpecification());
    when(streamView.get(newInputStream.getKey())).thenReturn(Optional.of(newInputStream));
    when(streamView.get(newOutputStream.getKey())).thenReturn(Optional.of(newOutputStream));
  }

  @Test
  public void create() {
    processService.create(entity);

    verify(processRepository).findById(entity.getKey());
    verify(processValidator).validateForCreate(entity);
    verify(handlerService).handleInsert(entity);
    verify(processRepository).saveSpecification(entity);
    verify(consumerService).canCreateConsumer(
      new Consumer(
        new ConsumerKey(
          inputStream.getKey().getDomainKey().getName(),
          inputStream.getKey().getName(),
          inputStream.getKey().getVersion(),
          zoneKey.getName(),
          entity.getKey().getName()
        ),
        new Specification(
          entity.getSpecification().getDescription(),
          entity.getSpecification().getTags(),
          entity.getSpecification().getType(),
          entity.getSpecification().getConfiguration(),
          inputStream.getSpecification().getSecurity(),
          entity.getSpecification().getFunction()
        ),
        entity.getStatus()
      )
    );
    verify(producerService).canCreateProducer(
      new Producer(
        new ProducerKey(
          outputStream.getKey().getDomainKey().getName(),
          outputStream.getKey().getName(),
          outputStream.getKey().getVersion(),
          zoneKey.getName(),
          entity.getKey().getName()
        ),
        new Specification(
          entity.getSpecification().getDescription(),
          entity.getSpecification().getTags(),
          entity.getSpecification().getType(),
          entity.getSpecification().getConfiguration(),
          outputStream.getSpecification().getSecurity(),
          entity.getSpecification().getFunction()
        ),
        entity.getStatus()
      )
    );
  }

  @Test(expected = ValidationException.class)
  public void createFailedWhenProcessNotExists() {
    when(processRepository.findById(entity.getKey())).thenReturn(Optional.empty());
    processService.update(newEntity);
  }

  @Test(expected = ValidationException.class)
  public void createFailedWhenValidationFails() {
    doThrow(ValidationException.class).when(processValidator).validateForCreate(entity);
    processService.create(entity);
  }

  @Test(expected = AccessDeniedException.class)
  public void createFailAuthConsumer() {
    when(consumerService.canCreateConsumer(any(Consumer.class))).thenThrow(AccessDeniedException.class);
    processService.create(entity);
  }

  @Test(expected = ValidationException.class)
  public void createFailInputNotExists() {
    when(streamView.get(inputStream.getKey())).thenReturn(Optional.empty());
    processService.create(entity);
  }

  @Test(expected = AccessDeniedException.class)
  public void createFailAuthProducer() {
    when(producerService.canCreateProducer(any(Producer.class))).thenThrow(AccessDeniedException.class);
    processService.create(entity);
  }

  @Test(expected = ValidationException.class)
  public void createFailOutputNotExists() {
    when(streamView.get(outputStream.getKey())).thenReturn(Optional.empty());
    processService.create(entity);
  }

  @Test
  public void updateNoInputOutputChanges() {
    when(processRepository.findById(entity.getKey())).thenReturn(Optional.of(entity));

    processService.update(entity);

    verify(processRepository).findById(entity.getKey());
    verify(processValidator).validateForUpdate(entity, entity);
    verify(handlerService).handleUpdate(entity, entity);
    verify(consumerService, never()).canCreateConsumer(any(Consumer.class));
    verify(producerService, never()).canCreateProducer(any(Producer.class));
    verify(processRepository).saveSpecification(entity);
  }

  @Test
  public void updateWithInputOutputChanges() {
    final Specification specification = mock(Specification.class);

    Stream newInputStream = createTestStream("streamInputName2");
    Stream newOutputStream = createTestStream("streamOutputName2");
    Process newEntity = createTestProcess(newInputStream.getKey(), newOutputStream.getKey());

    when(processRepository.findById(newEntity.getKey())).thenReturn(Optional.of(entity));
    when(streamView.get(newInputStream.getKey())).thenReturn(Optional.of(newInputStream));
    when(streamView.get(newOutputStream.getKey())).thenReturn(Optional.of(newOutputStream));
    doNothing().when(processValidator).validateForUpdate(newEntity, entity);
    when(handlerService.handleUpdate(newEntity, entity)).thenReturn(specification);
    when(processRepository.saveSpecification(newEntity)).thenReturn(newEntity);

    processService.update(newEntity);

    verify(processRepository).findById(entity.getKey());
    verify(processValidator).validateForUpdate(newEntity, entity);
    verify(handlerService).handleUpdate(newEntity, entity);
    verify(processRepository).saveSpecification(newEntity);
    verify(consumerService).canCreateConsumer(
      new Consumer(
        new ConsumerKey(
          newInputStream.getKey().getDomainKey().getName(),
          newInputStream.getKey().getName(),
          newInputStream.getKey().getVersion(),
          zoneKey.getName(),
          newEntity.getKey().getName()
        ),
        new Specification(
          newEntity.getSpecification().getDescription(),
          newEntity.getSpecification().getTags(),
          newEntity.getSpecification().getType(),
          newEntity.getSpecification().getConfiguration(),
          newInputStream.getSpecification().getSecurity(),
          newEntity.getSpecification().getFunction()
        ),
        newEntity.getStatus()
      )
    );
    verify(producerService).canCreateProducer(
      new Producer(
        new ProducerKey(
          newOutputStream.getKey().getDomainKey().getName(),
          newOutputStream.getKey().getName(),
          newOutputStream.getKey().getVersion(),
          zoneKey.getName(),
          newEntity.getKey().getName()
        ),
        new Specification(
          newEntity.getSpecification().getDescription(),
          newEntity.getSpecification().getTags(),
          newEntity.getSpecification().getType(),
          newEntity.getSpecification().getConfiguration(),
          newOutputStream.getSpecification().getSecurity(),
          newEntity.getSpecification().getFunction()
        ),
        newEntity.getStatus()
      )
    );
  }

  @Test(expected = ValidationException.class)
  public void updateValidationException() {
    when(processRepository.findById(newEntity.getKey())).thenReturn(Optional.of(entity));
    doThrow(ValidationException.class).when(processValidator).validateForUpdate(newEntity, entity);
    processService.update(newEntity);
  }

  @Test(expected = ValidationException.class)
  public void updateFailedWhenProcessNotExists() {
    when(processRepository.findById(newEntity.getKey())).thenReturn(Optional.empty());
    processService.update(newEntity);
  }

  @Test(expected = AccessDeniedException.class)
  public void updateFailAuthConsumer() {
    when(processRepository.findById(newEntity.getKey())).thenReturn(Optional.of(entity));
    when(consumerService.canCreateConsumer(any(Consumer.class))).thenThrow(AccessDeniedException.class);

    processService.update(newEntity);
  }

  @Test(expected = ValidationException.class)
  public void updateFailInputNotExists() {
    when(processRepository.findById(newEntity.getKey())).thenReturn(Optional.of(entity));
    when(streamView.get(newInputStream.getKey())).thenReturn(Optional.empty());
    processService.update(newEntity);
  }

  @Test(expected = AccessDeniedException.class)
  public void updateFailAuthProducer() {
    when(processRepository.findById(newEntity.getKey())).thenReturn(Optional.of(entity));
    when(producerService.canCreateProducer(any(Producer.class))).thenThrow(AccessDeniedException.class);

    processService.update(newEntity);
  }

  @Test(expected = ValidationException.class)
  public void updateFailOutputNotExists() {
    when(processRepository.findById(newEntity.getKey())).thenReturn(Optional.of(entity));
    when(streamView.get(newOutputStream.getKey())).thenReturn(Optional.empty());
    processService.update(newEntity);
  }

  @Test
  public void updateStatus() {
    final Status status = mock(Status.class);
    final Process entity = mock(Process.class);

    when(processRepository.saveStatus(entity)).thenReturn(entity);

    processService.updateStatus(entity, status);

    verify(processRepository).saveStatus(entity);
  }

  @Test
  public void delete() {
    final ProcessKey key = mock(ProcessKey.class);
    final Process entity = mock(Process.class);
    when(entity.getKey()).thenReturn(key);

    final ProcessBindingKey bindingKey = mock(ProcessBindingKey.class);
    final ProcessBinding binding = mock(ProcessBinding.class);
    when(bindingKey.getProcessKey()).thenReturn(key);
    when(binding.getKey()).thenReturn(bindingKey);

    when(processBindingRepository.findAll()).thenReturn(Collections.singletonList(binding));

    processService.delete(entity);

    InOrder inOrder = inOrder(processBindingService, processRepository);
    inOrder.verify(processBindingService).delete(binding);
    inOrder.verify(processRepository).delete(entity);
  }

  @Test
  public void delete_noBinding() {
    final Process entity = mock(Process.class);

    when(processBindingRepository.findAll()).thenReturn(emptyList());

    processService.delete(entity);

    InOrder inOrder = inOrder(processBindingService, processRepository);
    inOrder.verify(processBindingService, never()).delete(any());
    inOrder.verify(processRepository).delete(entity);
  }


  @Test
  public void delete_multi() {
    final ProcessKey key = mock(ProcessKey.class);
    final Process entity = mock(Process.class);
    when(entity.getKey()).thenReturn(key);

    final ProcessBindingKey bindingKey1 = mock(ProcessBindingKey.class);
    final ProcessBinding binding1 = mock(ProcessBinding.class);
    when(bindingKey1.getProcessKey()).thenReturn(key);
    when(binding1.getKey()).thenReturn(bindingKey1);

    final ProcessBindingKey bindingKey2 = mock(ProcessBindingKey.class);
    final ProcessBinding binding2 = mock(ProcessBinding.class);
    when(bindingKey2.getProcessKey()).thenReturn(key);
    when(binding2.getKey()).thenReturn(bindingKey2);

    when(processBindingRepository.findAll()).thenReturn(asList(binding1, binding2));

    processService.delete(entity);

    InOrder inOrder = inOrder(handlerService, processBindingService, processRepository);
    inOrder.verify(handlerService).handleDelete(entity);
    inOrder.verify(processBindingService).delete(binding1);
    inOrder.verify(processBindingService).delete(binding2);
    inOrder.verify(processRepository).delete(entity);
  }

  private Process createTestProcess(StreamKey input, StreamKey output) {
    Process p = new Process();
    List<ProcessInputStream> inputs = new ArrayList<>();
    ProcessInputStream pis = new ProcessInputStream(input, new ObjectMapper().createObjectNode());
    inputs.add(pis);
    p.setInputs(inputs);
    List<ProcessOutputStream> outputs = new ArrayList<>();
    ProcessOutputStream pos = new ProcessOutputStream(output, new ObjectMapper().createObjectNode());
    outputs.add(pos);
    p.setOutputs(outputs);
    p.setKey(new ProcessKey("domain","name"));
    List<ZoneKey> zones = new ArrayList<>();
    zones.add(zoneKey);
    p.setZones(zones);
    Specification specification = new Specification("", emptyList(), "generic", new ObjectMapper().createObjectNode(), emptyList(), "");
    p.setSpecification(specification);
    return p;
  }

  private Stream createTestStream(String name) {
    Stream stream = new Stream();
    Specification specification = new Specification(
      "",
      emptyList(),
      "egsp.kafka",
      new ObjectMapper().createObjectNode(),
      emptyList(),
      ""
    );

    stream.setKey(new StreamKey("domain", name, 1));
    stream.setSpecification(specification);

    return stream;
  }
}
