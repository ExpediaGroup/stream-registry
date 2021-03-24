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
import com.expediagroup.streamplatform.streamregistry.core.validators.ProducerValidator;
import com.expediagroup.streamplatform.streamregistry.core.views.ProducerBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProducerView;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;
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
  private ProducerBindingView producerBindingView;

  private ProducerService producerService;

  @Before
  public void before() {
    producerService = new ProducerService(
      handlerService,
      producerValidator,
      producerRepository,
      producerBindingService,
      producerBindingView,
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

    when(producerRepository.save(entity)).thenReturn(entity);

    producerService.create(entity);

    verify(entity).getKey();
    verify(producerRepository).findById(key);
    verify(producerValidator).validateForCreate(entity);
    verify(handlerService).handleInsert(entity);
    verify(producerRepository).save(entity);
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

    when(producerRepository.save(entity)).thenReturn(entity);

    producerService.update(entity);

    verify(entity).getKey();
    verify(producerRepository).findById(key);
    verify(producerValidator).validateForUpdate(entity, existingEntity);
    verify(handlerService).handleUpdate(entity, existingEntity);
    verify(producerRepository).save(entity);
  }

  @Test
  public void updateStatus() {
    final Status status = mock(Status.class);
    final Producer entity = mock(Producer.class);

    when(producerRepository.save(entity)).thenReturn(entity);

    producerService.updateStatus(entity, status);

    verify(producerRepository).save(entity);
  }

  @Test
  public void delete() {
    final Producer entity = mock(Producer.class);
    producerService.delete(entity);
    verify(producerRepository).delete(entity);
  }
}
