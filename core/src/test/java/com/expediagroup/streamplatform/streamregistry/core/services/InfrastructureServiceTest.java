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

import static org.mockito.ArgumentMatchers.any;
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
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.InfrastructureValidator;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.InfrastructureKey;
import com.expediagroup.streamplatform.streamregistry.repository.InfrastructureRepository;

@RunWith(MockitoJUnitRunner.class)
public class InfrastructureServiceTest {

  @Mock
  private HandlerService handlerService;

  @Mock
  private InfrastructureValidator infrastructureValidator;

  @Mock
  private InfrastructureRepository infrastructureRepository;

  private InfrastructureService infrastructureService;

  @Before
  public void before() {
    infrastructureService = new InfrastructureService(handlerService, infrastructureValidator, infrastructureRepository);
  }

  @Test
  public void create() {
    final Infrastructure entity = mock(Infrastructure.class);
    final InfrastructureKey key = mock(InfrastructureKey.class);
    final Specification specification = mock(Specification.class);

    Mockito.when(infrastructureRepository.findById(key)).thenReturn(Optional.empty());
    Mockito.doNothing().when(infrastructureValidator).validateForCreate(entity);
    Mockito.when(handlerService.handleInsert(entity)).thenReturn(specification);

    Mockito.when(infrastructureRepository.save(any())).thenReturn(entity);
    when(entity.getKey()).thenReturn(key);

    infrastructureService.create(entity);

    verify(entity).getKey();
    verify(infrastructureRepository).findById(key);
    verify(infrastructureValidator).validateForCreate(entity);
    verify(handlerService).handleInsert(entity);
    verify(infrastructureRepository).save(entity);
  }

  @Test
  public void update() {
    final Infrastructure entity = mock(Infrastructure.class);
    final InfrastructureKey key = mock(InfrastructureKey.class);
    final Infrastructure existingEntity = mock(Infrastructure.class);
    final Specification specification = mock(Specification.class);

    when(entity.getKey()).thenReturn(key);

    when(infrastructureRepository.findById(key)).thenReturn(Optional.of(existingEntity));
    doNothing().when(infrastructureValidator).validateForUpdate(entity, existingEntity);
    when(handlerService.handleUpdate(entity, existingEntity)).thenReturn(specification);

    when(infrastructureRepository.save(entity)).thenReturn(entity);

    infrastructureService.update(entity);

    verify(entity).getKey();
    verify(infrastructureRepository).findById(key);
    verify(infrastructureValidator).validateForUpdate(entity, existingEntity);
    verify(handlerService).handleUpdate(entity, existingEntity);
    verify(infrastructureRepository).save(entity);
  }

  @Test
  public void updateStatus() {
    final Infrastructure entity = mock(Infrastructure.class);
    final InfrastructureKey key = mock(InfrastructureKey.class);
    final Status status = mock(Status.class);

    when(infrastructureRepository.findById(key)).thenReturn(Optional.of(entity));
    when(infrastructureRepository.save(entity)).thenReturn(entity);

    infrastructureService.updateStatus(key, status);

    verify(infrastructureRepository).findById(key);
    verify(infrastructureRepository).save(entity);
  }

  @Test
  public void upsert() {
    final Infrastructure entity = mock(Infrastructure.class);
    final InfrastructureKey key = mock(InfrastructureKey.class);
    final Infrastructure existingEntity = mock(Infrastructure.class);
    final Specification specification = mock(Specification.class);

    when(entity.getKey()).thenReturn(key);

    when(infrastructureRepository.findById(key)).thenReturn(Optional.of(existingEntity));
    doNothing().when(infrastructureValidator).validateForUpdate(entity, existingEntity);
    when(handlerService.handleUpdate(entity, existingEntity)).thenReturn(specification);

    when(infrastructureRepository.save(entity)).thenReturn(entity);

    infrastructureService.upsert(entity);

    verify(entity, times(2)).getKey();
    verify(infrastructureRepository, times(2)).findById(key);
    verify(infrastructureValidator).validateForUpdate(entity, existingEntity);
    verify(handlerService).handleUpdate(entity, existingEntity);
    verify(infrastructureRepository).save(entity);
  }
}