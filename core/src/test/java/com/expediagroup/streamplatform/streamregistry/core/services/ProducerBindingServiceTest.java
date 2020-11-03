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
import com.expediagroup.streamplatform.streamregistry.core.validators.ProducerBindingValidator;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerBindingKey;
import com.expediagroup.streamplatform.streamregistry.repository.ProducerBindingRepository;

@RunWith(MockitoJUnitRunner.class)
public class ProducerBindingServiceTest {

  @Mock
  private HandlerService handlerService;

  @Mock
  private ProducerBindingValidator producerBindingValidator;

  @Mock
  private ProducerBindingRepository producerBindingRepository;

  private ProducerBindingService producerBindingService;

  @Before
  public void before() {
    producerBindingService = new ProducerBindingService(handlerService, producerBindingValidator, producerBindingRepository);
  }

  @Test
  public void create() {
    final ProducerBindingKey key = mock(ProducerBindingKey.class);
    final Specification specification = mock(Specification.class);

    final ProducerBinding entity = mock(ProducerBinding.class);
    when(entity.getKey()).thenReturn(key);
    when(producerBindingRepository.findById(key)).thenReturn(Optional.empty());

    doNothing().when(producerBindingValidator).validateForCreate(entity);
    when(handlerService.handleInsert(entity)).thenReturn(specification);

    when(producerBindingRepository.save(entity)).thenReturn(entity);

    producerBindingService.create(entity);

    verify(entity).getKey();
    verify(producerBindingRepository).findById(key);
    verify(producerBindingValidator).validateForCreate(entity);
    verify(handlerService).handleInsert(entity);
    verify(producerBindingRepository).save(entity);
  }

  @Test
  public void update() {
    final ProducerBindingKey key = mock(ProducerBindingKey.class);
    final Specification specification = mock(Specification.class);

    final ProducerBinding entity = mock(ProducerBinding.class);
    final ProducerBinding existingEntity = mock(ProducerBinding.class);

    when(entity.getKey()).thenReturn(key);

    when(producerBindingRepository.findById(key)).thenReturn(Optional.of(existingEntity));
    doNothing().when(producerBindingValidator).validateForUpdate(entity, existingEntity);
    when(handlerService.handleUpdate(entity, existingEntity)).thenReturn(specification);

    when(producerBindingRepository.save(entity)).thenReturn(entity);

    producerBindingService.update(entity);

    verify(entity).getKey();
    verify(producerBindingRepository).findById(key);
    verify(producerBindingValidator).validateForUpdate(entity, existingEntity);
    verify(handlerService).handleUpdate(entity, existingEntity);
    verify(producerBindingRepository).save(entity);
  }

  @Test
  public void updateStatus() {
    final Status status = mock(Status.class);
    final ProducerBinding entity = mock(ProducerBinding.class);

    when(producerBindingRepository.save(entity)).thenReturn(entity);

    producerBindingService.updateStatus(entity, status);

    verify(producerBindingRepository).save(entity);
  }
}