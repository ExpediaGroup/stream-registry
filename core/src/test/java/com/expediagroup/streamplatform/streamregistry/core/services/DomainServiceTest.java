/**
 * Copyright (C) 2018-2022 Expedia, Inc.
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
import com.expediagroup.streamplatform.streamregistry.core.validators.DomainValidator;
import com.expediagroup.streamplatform.streamregistry.core.views.DomainView;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.DomainKey;
import com.expediagroup.streamplatform.streamregistry.repository.DomainRepository;

@RunWith(MockitoJUnitRunner.class)
public class DomainServiceTest {

  @Mock
  private HandlerService handlerService;

  @Mock
  private DomainValidator domainValidator;

  @Mock
  private DomainRepository domainRepository;

  private DomainService domainService;

  @Before
  public void before() {
    domainService = new DomainService(
      new DomainView(domainRepository),
      handlerService,
      domainValidator,
      domainRepository
    );
  }

  @Test
  public void create() {
    final Domain entity = mock(Domain.class);
    final DomainKey key = mock(DomainKey.class);
    final Specification specification = mock(Specification.class);

    Mockito.when(domainRepository.findById(key)).thenReturn(Optional.empty());
    Mockito.doNothing().when(domainValidator).validateForCreate(entity);
    Mockito.when(handlerService.handleInsert(entity)).thenReturn(specification);

    Mockito.when(domainRepository.save(any())).thenReturn(entity);
    when(entity.getKey()).thenReturn(key);

    domainService.create(entity);

    verify(entity).getKey();
    verify(domainRepository).findById(key);
    verify(domainValidator).validateForCreate(entity);
    verify(handlerService).handleInsert(entity);
    verify(domainRepository).save(entity);
  }

  @Test
  public void update() {
    final Domain entity = mock(Domain.class);
    final DomainKey key = mock(DomainKey.class);
    final Domain existingEntity = mock(Domain.class);
    final Specification specification = mock(Specification.class);

    when(entity.getKey()).thenReturn(key);

    when(domainRepository.findById(key)).thenReturn(Optional.of(existingEntity));
    doNothing().when(domainValidator).validateForUpdate(entity, existingEntity);
    when(handlerService.handleUpdate(entity, existingEntity)).thenReturn(specification);

    when(domainRepository.save(entity)).thenReturn(entity);

    domainService.update(entity);

    verify(entity).getKey();
    verify(domainRepository).findById(key);
    verify(domainValidator).validateForUpdate(entity, existingEntity);
    verify(handlerService).handleUpdate(entity, existingEntity);
    verify(domainRepository).save(entity);
  }

  @Test
  public void updateStatus() {
    final Domain entity = mock(Domain.class);
    final Status status = mock(Status.class);

    when(domainRepository.save(entity)).thenReturn(entity);

    domainService.updateStatus(entity, status);

    verify(domainRepository).save(entity);
  }

  public void delete() {
    final Domain entity = mock(Domain.class);
    domainService.delete(entity);
    verify(domainRepository).delete(entity);
  }
}
