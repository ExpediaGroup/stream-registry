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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.Process;
import com.expediagroup.streamplatform.streamregistry.model.ProcessBinding;
import com.expediagroup.streamplatform.streamregistry.model.ProcessInputStream;
import com.expediagroup.streamplatform.streamregistry.model.ProcessOutputStream;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProcessBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProcessKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;
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
      producerService
      );
  }

  @Test
  public void create() {
    final Specification specification = mock(Specification.class);
    Process entity = createTestProcess();

    when(processRepository.findById(entity.getKey())).thenReturn(empty());
    doNothing().when(processValidator).validateForCreate(entity);
    when(handlerService.handleInsert(entity)).thenReturn(specification);
    when(processRepository.saveSpecification(entity)).thenReturn(entity);

    processService.create(entity);

    verify(processRepository).findById(entity.getKey());
    verify(processValidator).validateForCreate(entity);
    verify(handlerService).handleInsert(entity);
    verify(processRepository).saveSpecification(entity);
  }

  @Test(expected = ValidationException.class)
  public void createFailedWhenDomainNotExists() {
    Process entity = createTestProcess();
    when(processRepository.findById(entity.getKey())).thenReturn(empty());
    doThrow(ValidationException.class).when(processValidator).validateForCreate(entity);
    processService.create(entity);
  }

  @Test(expected = AccessDeniedException.class)
  public void createFailAuthConsumer() {
    Process p = createTestProcess();
    when(consumerService.canCreateConsumer(any(Consumer.class))).thenThrow(AccessDeniedException.class);
    processService.create(p);
  }

  @Test(expected = AccessDeniedException.class)
  public void createFailAuthProducer() {
    Process p = createTestProcess();
    when(producerService.canCreateProducer(any(Producer.class))).thenThrow(AccessDeniedException.class);
    processService.create(p);
  }

  @Test
  public void updateNoInputOutputChanges() {
    final Specification specification = mock(Specification.class);

    Process existing = createTestProcess();
    Process newEntity = createTestProcess();

    when(processRepository.findById(existing.getKey())).thenReturn(Optional.of(existing));
    doNothing().when(processValidator).validateForUpdate(newEntity, existing);
    when(handlerService.handleUpdate(newEntity, existing)).thenReturn(specification);
    when(processRepository.saveSpecification(newEntity)).thenReturn(newEntity);

    processService.update(newEntity);

    verify(processRepository).findById(existing.getKey());
    verify(processValidator).validateForUpdate(newEntity, existing);
    verify(handlerService).handleUpdate(newEntity, existing);
    verify(consumerService, never()).canCreateConsumer(any(Consumer.class));
    verify(producerService, never()).canCreateProducer(any(Producer.class));
    verify(processRepository).saveSpecification(newEntity);
  }

  @Test
  public void updateWithInputOutputChanges() {
    final Specification specification = mock(Specification.class);

    Process existing = createTestProcess();
    Process newEntity = createTestProcess();
    ProcessInputStream pis = new ProcessInputStream(new StreamKey("inputDomain","streamInputName2",1), new ObjectMapper().createObjectNode());
    newEntity.getInputs().add(pis);
    ProcessOutputStream pos = new ProcessOutputStream(new StreamKey("outputDomain","streamOutputName2",1), new ObjectMapper().createObjectNode());
    newEntity.getOutputs().add(pos);

    when(processRepository.findById(existing.getKey())).thenReturn(Optional.of(existing));
    doNothing().when(processValidator).validateForUpdate(newEntity, existing);
    when(handlerService.handleUpdate(newEntity, existing)).thenReturn(specification);
    when(processRepository.saveSpecification(newEntity)).thenReturn(newEntity);

    processService.update(newEntity);

    verify(processRepository).findById(existing.getKey());
    verify(processValidator).validateForUpdate(newEntity, existing);
    verify(handlerService).handleUpdate(newEntity, existing);
    verify(consumerService, times(1)).canCreateConsumer(any(Consumer.class));
    verify(producerService, times(1)).canCreateProducer(any(Producer.class));
    verify(processRepository).saveSpecification(newEntity);
  }

  @Test(expected = ValidationException.class)
  public void updateValidationException() {
    Process p = createTestProcess();
    processService.update(p);
  }

  @Test(expected = ValidationException.class)
  public void updateFailedWhenDomainNotExists() {
    Process entity = createTestProcess();
    Process existing = createTestProcess();
    when(processRepository.findById(existing.getKey())).thenReturn(Optional.of(existing));
    doThrow(ValidationException.class).when(processValidator).validateForUpdate(entity, existing);
    processService.update(entity);
  }

  @Test(expected = AccessDeniedException.class)
  public void updateFailAuthConsumer() {
    final Specification specification = mock(Specification.class);

    Process existing = createTestProcess();
    Process newEntity = createTestProcess();

    ProcessInputStream pis = new ProcessInputStream(new StreamKey("inputDomain","streamInputName2",1), new ObjectMapper().createObjectNode());
    newEntity.getInputs().add(pis);

    when(processRepository.findById(existing.getKey())).thenReturn(Optional.of(existing));
    doNothing().when(processValidator).validateForUpdate(newEntity, existing);
    when(handlerService.handleUpdate(newEntity, existing)).thenReturn(specification);
    when(consumerService.canCreateConsumer(any(Consumer.class))).thenThrow(AccessDeniedException.class);

    processService.update(newEntity);
  }

  @Test(expected = AccessDeniedException.class)
  public void updateFailAuthProducer() {
    final Specification specification = mock(Specification.class);

    Process existing = createTestProcess();
    Process newEntity = createTestProcess();
    ProcessOutputStream pos = new ProcessOutputStream(new StreamKey("outputDomain","streamOutputName2",1), new ObjectMapper().createObjectNode());
    newEntity.getOutputs().add(pos);

    when(processRepository.findById(existing.getKey())).thenReturn(Optional.of(existing));
    doNothing().when(processValidator).validateForUpdate(newEntity, existing);
    when(handlerService.handleUpdate(newEntity, existing)).thenReturn(specification);
    when(producerService.canCreateProducer(any(Producer.class))).thenThrow(AccessDeniedException.class);

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

  private Process createTestProcess() {
    Process p = new Process();
    List<ProcessInputStream> inputs = new ArrayList<>();
    ProcessInputStream pis = new ProcessInputStream(new StreamKey("inputDomain","streamInputName",1), new ObjectMapper().createObjectNode());
    inputs.add(pis);
    p.setInputs(inputs);
    List<ProcessOutputStream> outputs = new ArrayList<>();
    ProcessOutputStream pos = new ProcessOutputStream(new StreamKey("outputDomain","streamOutputName",1), new ObjectMapper().createObjectNode());
    outputs.add(pos);
    p.setOutputs(outputs);
    p.setKey(new ProcessKey("domain","name"));
    List<ZoneKey> zones = new ArrayList<>();
    zones.add(new ZoneKey("aws_us_east_1"));
    p.setZones(zones);
    return p;
  }
}
