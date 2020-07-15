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
package com.expediagroup.streamplatform.streamregistry.core.events;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.services.ConsumerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.ConsumerValidator;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.repository.ConsumerRepository;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerServiceTest {

  @Mock
  private HandlerService handlerService;

  @Mock
  private ConsumerValidator consumerValidator;

  @Mock
  private ConsumerRepository consumerRepository;

  private ConsumerService consumerService;

  @Before
  public void before() {
    consumerService = new ConsumerService(handlerService, consumerValidator, consumerRepository);
  }

  @Test
  public void create() {
    final ConsumerKey key = mock(ConsumerKey.class);
    final Specification specification = mock(Specification.class);

    final Consumer entity = mock(Consumer.class);
    when(entity.getKey()).thenReturn(key);
    when(consumerRepository.findById(key)).thenReturn(Optional.empty());

    doNothing().when(consumerValidator).validateForCreate(entity);
    when(handlerService.handleInsert(entity)).thenReturn(specification);

    when(consumerRepository.save(entity)).thenReturn(entity);

    consumerService.create(entity);

    verify(entity).getKey();
    verify(consumerRepository).findById(key);
    verify(consumerValidator).validateForCreate(entity);
    verify(handlerService).handleInsert(entity);
    verify(consumerRepository).save(entity);
  }

  @Test
  public void update() {
    final ConsumerKey key = mock(ConsumerKey.class);
    final Specification specification = mock(Specification.class);

    final Consumer entity = mock(Consumer.class);
    final Consumer existingEntity = mock(Consumer.class);

    when(entity.getKey()).thenReturn(key);


    when(consumerRepository.findById(key)).thenReturn(Optional.of(existingEntity));
    doNothing().when(consumerValidator).validateForUpdate(entity, existingEntity);
    when(handlerService.handleUpdate(entity, existingEntity)).thenReturn(specification);

    when(consumerRepository.save(entity)).thenReturn(entity);

    consumerService.update(entity);

    verify(entity).getKey();
    verify(consumerRepository).findById(key);
    verify(consumerValidator).validateForUpdate(entity, existingEntity);
    verify(handlerService).handleUpdate(entity, existingEntity);
    verify(consumerRepository).save(entity);
  }

  @Test
  public void upsert() {
    final ConsumerKey key = mock(ConsumerKey.class);
    final Specification specification = mock(Specification.class);

    final Consumer entity = mock(Consumer.class);
    final Consumer existingEntity = mock(Consumer.class);

    when(entity.getKey()).thenReturn(key);


    when(consumerRepository.findById(key)).thenReturn(Optional.of(existingEntity));
    doNothing().when(consumerValidator).validateForUpdate(entity, existingEntity);
    when(handlerService.handleUpdate(entity, existingEntity)).thenReturn(specification);

    when(consumerRepository.save(entity)).thenReturn(entity);

    consumerService.upsert(entity);

    verify(entity, times(2)).getKey();
    verify(consumerRepository, times(2)).findById(key);
    verify(consumerValidator).validateForUpdate(entity, existingEntity);
    verify(handlerService).handleUpdate(entity, existingEntity);
    verify(consumerRepository).save(entity);
  }
}