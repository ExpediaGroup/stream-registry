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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.ProcessValidator;
import com.expediagroup.streamplatform.streamregistry.core.views.*;
import com.expediagroup.streamplatform.streamregistry.model.Process;
import com.expediagroup.streamplatform.streamregistry.model.ProcessBinding;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.*;
import com.expediagroup.streamplatform.streamregistry.repository.*;

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
      new ProcessView(processRepository)
      );
  }

  @Test
  public void create() {
    final ProcessKey key = mock(ProcessKey.class);
    final Specification specification = mock(Specification.class);

    final Process entity = mock(Process.class);
    when(entity.getKey()).thenReturn(key);
    when(processRepository.findById(key)).thenReturn(empty());

    doNothing().when(processValidator).validateForCreate(entity);
    when(handlerService.handleInsert(entity)).thenReturn(specification);

    when(processRepository.save(entity)).thenReturn(entity);

    processService.create(entity);

    verify(entity).getKey();
    verify(processRepository).findById(key);
    verify(processValidator).validateForCreate(entity);
    verify(handlerService).handleInsert(entity);
    verify(processRepository).save(entity);
  }

  @Test
  public void update() {
    final ProcessKey key = mock(ProcessKey.class);
    final Specification specification = mock(Specification.class);

    final Process entity = mock(Process.class);
    final Process existingEntity = mock(Process.class);

    when(entity.getKey()).thenReturn(key);

    when(processRepository.findById(key)).thenReturn(Optional.of(existingEntity));
    doNothing().when(processValidator).validateForUpdate(entity, existingEntity);
    when(handlerService.handleUpdate(entity, existingEntity)).thenReturn(specification);

    when(processRepository.save(entity)).thenReturn(entity);

    processService.update(entity);

    verify(entity).getKey();
    verify(processRepository).findById(key);
    verify(processValidator).validateForUpdate(entity, existingEntity);
    verify(handlerService).handleUpdate(entity, existingEntity);
    verify(processRepository).save(entity);
  }

  @Test
  public void updateStatus() {
    final Status status = mock(Status.class);
    final Process entity = mock(Process.class);

    when(processRepository.save(entity)).thenReturn(entity);

    processService.updateStatus(entity, status);

    verify(processRepository).save(entity);
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

    InOrder inOrder = inOrder(processBindingService, processRepository);
    inOrder.verify(processBindingService).delete(binding1);
    inOrder.verify(processBindingService).delete(binding2);
    inOrder.verify(processRepository).delete(entity);
  }
}
