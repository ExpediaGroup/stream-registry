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
import com.expediagroup.streamplatform.streamregistry.core.validators.SchemaValidator;
import com.expediagroup.streamplatform.streamregistry.core.views.SchemaView;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.repository.SchemaRepository;

@RunWith(MockitoJUnitRunner.class)
public class SchemaServiceTest {

  @Mock
  private HandlerService handlerService;

  @Mock
  private SchemaValidator schemaValidator;

  @Mock
  private SchemaRepository schemaRepository;

  private SchemaService schemaService;

  @Before
  public void before() {
    schemaService = new SchemaService(
      handlerService,
      schemaValidator,
      schemaRepository,
      new SchemaView(schemaRepository)
    );
  }

  @Test
  public void create() {
    final SchemaKey key = mock(SchemaKey.class);
    final Specification specification = mock(Specification.class);

    final Schema entity = mock(Schema.class);
    when(entity.getKey()).thenReturn(key);

    doNothing().when(schemaValidator).validateForCreate(entity);
    when(handlerService.handleInsert(entity)).thenReturn(specification);

    when(schemaRepository.save(entity)).thenReturn(entity);

    schemaService.create(entity);

    verify(entity).getKey();
    verify(schemaValidator).validateForCreate(entity);
    verify(handlerService).handleInsert(entity);
    verify(schemaRepository).save(entity);
  }

  @Test
  public void update() {
    final Schema entity = mock(Schema.class);
    final SchemaKey key = mock(SchemaKey.class);
    final Schema existingEntity = mock(Schema.class);
    final Specification specification = mock(Specification.class);

    when(entity.getKey()).thenReturn(key);

    when(schemaRepository.findById(key)).thenReturn(Optional.of(existingEntity));
    doNothing().when(schemaValidator).validateForUpdate(entity, existingEntity);
    when(handlerService.handleUpdate(entity, existingEntity)).thenReturn(specification);

    when(schemaRepository.save(entity)).thenReturn(entity);

    schemaService.update(entity);

    verify(entity).getKey();
    verify(schemaRepository).findById(key);
    verify(schemaValidator).validateForUpdate(entity, existingEntity);
    verify(handlerService).handleUpdate(entity, existingEntity);
    verify(schemaRepository).save(entity);
  }

  @Test
  public void updateStatus() {
    final Schema entity = mock(Schema.class);
    final Status status = mock(Status.class);

    when(schemaRepository.save(entity)).thenReturn(entity);

    schemaService.updateStatus(entity, status);

    verify(schemaRepository).save(entity);
  }

  @Test
  public void delete() {
    final Schema entity = mock(Schema.class);
    schemaService.delete(entity);
    verify(schemaRepository).delete(entity);
  }
}
