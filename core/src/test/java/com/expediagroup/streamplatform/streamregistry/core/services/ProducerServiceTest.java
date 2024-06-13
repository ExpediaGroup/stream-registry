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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.ProducerValidator;
import com.expediagroup.streamplatform.streamregistry.core.views.ProducerBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProducerView;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.repository.ProducerBindingRepository;
import com.expediagroup.streamplatform.streamregistry.repository.ProducerRepository;

@RunWith(MockitoJUnitRunner.class)
public class ProducerServiceTest {

  @Mock
  private HandlerService handlerService;

  @Mock
  private ProducerValidator producerValidator;

  @Mock
  private ProducerRepository producerRepository;

  @Mock
  private  ProducerBindingService producerBindingService;

  @Mock
  private ProducerBindingRepository producerBindingRepository;

  private ProducerService producerService;

  @Before
  public void before() {
    producerService = new ProducerService(
      handlerService,
      producerValidator,
      producerRepository,
      producerBindingService,
      new ProducerBindingView(producerBindingRepository),
      new ProducerView(producerRepository)
    );
  }

  @Test
  public void create() {
    final ProducerKey key = mock(ProducerKey.class);
    final Specification specification = mock(Specification.class);

    final Producer entity = mock(Producer.class);
    when(entity.getKey()).thenReturn(key);
    when(producerRepository.findById(key)).thenReturn(Optional.empty());

    doNothing().when(producerValidator).validateForCreate(entity);
    when(handlerService.handleInsert(entity)).thenReturn(specification);

    when(producerRepository.saveSpecification(entity)).thenReturn(entity);

    producerService.create(entity);

    verify(entity).getKey();
    verify(producerRepository).findById(key);
    verify(producerValidator).validateForCreate(entity);
    verify(handlerService).handleInsert(entity);
    verify(producerRepository).saveSpecification(entity);
  }

  @Test
  public void update() {
    final ProducerKey key = mock(ProducerKey.class);
    final Specification specification = mock(Specification.class);

    final Producer entity = mock(Producer.class);
    final Producer existingEntity = mock(Producer.class);

    when(entity.getKey()).thenReturn(key);

    when(producerRepository.findById(key)).thenReturn(Optional.of(existingEntity));
    doNothing().when(producerValidator).validateForUpdate(entity, existingEntity);
    when(handlerService.handleUpdate(entity, existingEntity)).thenReturn(specification);

    when(producerRepository.saveSpecification(entity)).thenReturn(entity);

    producerService.update(entity);

    verify(entity).getKey();
    verify(producerRepository).findById(key);
    verify(producerValidator).validateForUpdate(entity, existingEntity);
    verify(handlerService).handleUpdate(entity, existingEntity);
    verify(producerRepository).saveSpecification(entity);
  }

  @Test
  public void updateStatus() {
    final Status status = mock(Status.class);
    final Producer entity = mock(Producer.class);

    when(producerRepository.saveStatus(entity)).thenReturn(entity);

    producerService.updateStatus(entity, status);

    verify(producerRepository).saveStatus(entity);
  }

  @Test
  public void delete() {
    final ProducerKey key = mock(ProducerKey.class);
    final Producer entity = mock(Producer.class);
    when(entity.getKey()).thenReturn(key);

    final ProducerBindingKey bindingKey = mock(ProducerBindingKey.class);
    final ProducerBinding binding = mock(ProducerBinding.class);
    when(bindingKey.getProducerKey()).thenReturn(key);
    when(binding.getKey()).thenReturn(bindingKey);

    when(producerBindingRepository.findAll()).thenReturn(List.of(binding));

    producerService.delete(entity);

    verify(producerBindingService).delete(binding);
    verify(producerRepository).delete(entity);
  }

  @Test
  public void delete_noChildren() {
    final Producer entity = mock(Producer.class);

    when(producerBindingRepository.findAll()).thenReturn(emptyList());

    producerService.delete(entity);

    verify(producerBindingService, never()).delete(any());
    verify(producerRepository).delete(entity);
  }

  @Test
  public void delete_multi() {
    final ProducerKey key = mock(ProducerKey.class);
    final Producer entity = mock(Producer.class);
    when(entity.getKey()).thenReturn(key);

    final ProducerBindingKey bindingKey1 = mock(ProducerBindingKey.class);
    final ProducerBinding binding1 = mock(ProducerBinding.class);
    when(bindingKey1.getProducerKey()).thenReturn(key);
    when(binding1.getKey()).thenReturn(bindingKey1);

    final ProducerBindingKey bindingKey2 = mock(ProducerBindingKey.class);
    final ProducerBinding binding2 = mock(ProducerBinding.class);
    when(bindingKey2.getProducerKey()).thenReturn(key);
    when(binding2.getKey()).thenReturn(bindingKey2);

    when(producerBindingRepository.findAll()).thenReturn(asList(binding1, binding2));

    producerService.delete(entity);

    verify(producerBindingService).delete(binding1);
    verify(producerBindingService).delete(binding2);
    verify(producerRepository).delete(entity);
  }

}
