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
package com.expediagroup.streamplatform.streamregistry.core.services;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.ConsumerBindingValidator;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerBindingKey;
import com.expediagroup.streamplatform.streamregistry.repository.ConsumerBindingRepository;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerBindingServiceTest {

  @Mock
  private HandlerService handlerService;

  @Mock
  private ConsumerBindingValidator consumerBindingValidator;

  @Mock
  private ConsumerBindingRepository consumerBindingRepository;

  private ConsumerBindingService consumerBindingService;

  @Before
  public void before() {
    consumerBindingService = new ConsumerBindingService(handlerService, consumerBindingValidator, consumerBindingRepository);
  }

  @Test
  public void create() {
    final ConsumerBindingKey key = mock(ConsumerBindingKey.class);
    final Specification specification = mock(Specification.class);

    final ConsumerBinding entity = mock(ConsumerBinding.class);
    when(entity.getKey()).thenReturn(key);
    when(consumerBindingRepository.findById(key)).thenReturn(Optional.empty());

    doNothing().when(consumerBindingValidator).validateForCreate(entity);
    when(handlerService.handleInsert(entity)).thenReturn(specification);

    when(consumerBindingRepository.save(entity)).thenReturn(entity);

    consumerBindingService.create(entity);

    verify(entity).getKey();
    verify(consumerBindingRepository).findById(key);
    verify(consumerBindingValidator).validateForCreate(entity);
    verify(handlerService).handleInsert(entity);
    verify(consumerBindingRepository).save(entity);
  }

  @Test
  public void update() {
    final ConsumerBindingKey key = mock(ConsumerBindingKey.class);
    final Specification specification = mock(Specification.class);

    final ConsumerBinding entity = mock(ConsumerBinding.class);
    final ConsumerBinding existingEntity = mock(ConsumerBinding.class);

    when(entity.getKey()).thenReturn(key);

    when(consumerBindingRepository.findById(key)).thenReturn(Optional.of(existingEntity));
    doNothing().when(consumerBindingValidator).validateForUpdate(entity, existingEntity);
    when(handlerService.handleUpdate(entity, existingEntity)).thenReturn(specification);

    when(consumerBindingRepository.save(entity)).thenReturn(entity);

    consumerBindingService.update(entity);

    verify(entity).getKey();
    verify(consumerBindingRepository).findById(key);
    verify(consumerBindingValidator).validateForUpdate(entity, existingEntity);
    verify(handlerService).handleUpdate(entity, existingEntity);
    verify(consumerBindingRepository).save(entity);
  }

  @Test
  public void updateStatus() {
    final ConsumerBindingKey key = mock(ConsumerBindingKey.class);
    final Status status = mock(Status.class);
    final ConsumerBinding entity = mock(ConsumerBinding.class);

    when(consumerBindingRepository.findById(key)).thenReturn(Optional.of(entity));
    when(consumerBindingRepository.save(entity)).thenReturn(entity);

    consumerBindingService.updateStatus(key, status);

    verify(consumerBindingRepository).findById(key);
    verify(consumerBindingRepository).save(entity);
  }
}