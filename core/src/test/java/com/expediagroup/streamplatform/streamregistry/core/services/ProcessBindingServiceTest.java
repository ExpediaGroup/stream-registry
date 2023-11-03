/**
 * Copyright (C) 2018-2021 Expedia, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.core.services;


import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.ProcessBindingValidator;
import com.expediagroup.streamplatform.streamregistry.core.views.ProcessBindingView;
import com.expediagroup.streamplatform.streamregistry.model.ProcessBinding;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProcessBindingKey;
import com.expediagroup.streamplatform.streamregistry.repository.ProcessBindingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProcessBindingServiceTest {

  @Mock
  private HandlerService handlerService;

  @Mock
  private ProcessBindingValidator processBindingValidator;

  @Mock
  private ProcessBindingRepository processBindingRepository;

  private ProcessBindingService processBindingService;

  private ObjectMapper objectMapper = new ObjectMapper();

  @Before
  public void before() {
    processBindingService = new ProcessBindingService(
      new ProcessBindingView(processBindingRepository),
      handlerService,
      processBindingValidator,
      processBindingRepository
    );
  }

  @Test
  public void create() {
    final ProcessBindingKey key = mock(ProcessBindingKey.class);
    final Specification specification = mock(Specification.class);

    final ProcessBinding entity = mock(ProcessBinding.class);
    when(entity.getKey()).thenReturn(key);
    when(processBindingRepository.findById(key)).thenReturn(Optional.empty());

    doNothing().when(processBindingValidator).validateForCreate(entity);
    when(handlerService.handleInsert(entity)).thenReturn(specification);

    when(processBindingRepository.save(entity)).thenReturn(entity);

    processBindingService.create(entity);

    verify(entity).getKey();
    verify(processBindingRepository).findById(key);
    verify(processBindingValidator).validateForCreate(entity);
    verify(handlerService).handleInsert(entity);
    verify(processBindingRepository).save(entity);
  }

  @Test
  public void update() {
    final ProcessBindingKey key = mock(ProcessBindingKey.class);
    final Specification specification = mock(Specification.class);

    final ProcessBinding entity = mock(ProcessBinding.class);
    final ProcessBinding existingEntity = mock(ProcessBinding.class);

    when(entity.getKey()).thenReturn(key);

    when(processBindingRepository.findById(key)).thenReturn(Optional.of(existingEntity));
    doNothing().when(processBindingValidator).validateForUpdate(entity, existingEntity);
    when(handlerService.handleUpdate(entity, existingEntity)).thenReturn(specification);

    when(processBindingRepository.save(entity)).thenReturn(entity);

    processBindingService.update(entity);

    verify(entity).getKey();
    verify(processBindingRepository).findById(key);
    verify(processBindingValidator).validateForUpdate(entity, existingEntity);
    verify(handlerService).handleUpdate(entity, existingEntity);
    verify(processBindingRepository).save(entity);
  }

  @Test
  public void updateStatus() {
    final Status status = mock(Status.class);
    final ProcessBinding entity = mock(ProcessBinding.class);

    when(processBindingRepository.save(entity)).thenReturn(entity);

    processBindingService.updateStatus(entity, status);

    verify(processBindingRepository).save(entity);
  }

  @Test
  public void delete() {
    final ProcessBinding entity = mock(ProcessBinding.class);

    processBindingService.delete(entity);

    InOrder inOrder = inOrder(handlerService, processBindingRepository);
    inOrder.verify(handlerService).handleDelete(entity);
    inOrder.verify(processBindingRepository).delete(entity);

  }
}
