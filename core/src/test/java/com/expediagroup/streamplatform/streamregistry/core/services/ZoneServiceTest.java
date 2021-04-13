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
import com.expediagroup.streamplatform.streamregistry.core.validators.ZoneValidator;
import com.expediagroup.streamplatform.streamregistry.core.views.ZoneView;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Zone;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;
import com.expediagroup.streamplatform.streamregistry.repository.ZoneRepository;

@RunWith(MockitoJUnitRunner.class)
public class ZoneServiceTest {

  @Mock
  private HandlerService handlerService;

  @Mock
  private ZoneValidator zoneValidator;

  @Mock
  private ZoneRepository zoneRepository;

  private ZoneService zoneService;

  @Before
  public void before() {
    zoneService = new ZoneService(
      handlerService,
      zoneValidator,
      zoneRepository,
      new ZoneView(zoneRepository)
    );
  }

  @org.junit.Test
  public void create() {
    final Zone entity = mock(Zone.class);
    final ZoneKey key = mock(ZoneKey.class);
    final Specification specification = mock(Specification.class);

    Mockito.when(zoneRepository.findById(key)).thenReturn(Optional.empty());
    Mockito.doNothing().when(zoneValidator).validateForCreate(entity);
    Mockito.when(handlerService.handleInsert(entity)).thenReturn(specification);

    Mockito.when(zoneRepository.save(any())).thenReturn(entity);
    when(entity.getKey()).thenReturn(key);

    zoneService.create(entity);

    verify(entity).getKey();
    verify(zoneRepository).findById(key);
    verify(zoneValidator).validateForCreate(entity);
    verify(handlerService).handleInsert(entity);
    verify(zoneRepository).save(entity);
  }

  @org.junit.Test
  public void update() {
    final Zone entity = mock(Zone.class);
    final ZoneKey key = mock(ZoneKey.class);
    final Zone existingEntity = mock(Zone.class);
    final Specification specification = mock(Specification.class);

    when(entity.getKey()).thenReturn(key);

    when(zoneRepository.findById(key)).thenReturn(Optional.of(existingEntity));
    doNothing().when(zoneValidator).validateForUpdate(entity, existingEntity);
    when(handlerService.handleUpdate(entity, existingEntity)).thenReturn(specification);

    when(zoneRepository.save(entity)).thenReturn(entity);

    zoneService.update(entity);

    verify(entity).getKey();
    verify(zoneRepository).findById(key);
    verify(zoneValidator).validateForUpdate(entity, existingEntity);
    verify(handlerService).handleUpdate(entity, existingEntity);
    verify(zoneRepository).save(entity);
  }

  @org.junit.Test
  public void updateStatus() {
    final Zone entity = mock(Zone.class);
    final Status status = mock(Status.class);

    when(zoneRepository.save(entity)).thenReturn(entity);

    zoneService.updateStatus(entity, status);

    verify(zoneRepository).save(entity);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void delete() {
    final Zone entity = mock(Zone.class);
    zoneService.delete(entity);
  }
}
