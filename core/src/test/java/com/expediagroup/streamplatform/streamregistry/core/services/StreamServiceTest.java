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
import com.expediagroup.streamplatform.streamregistry.core.validators.StreamValidator;
import com.expediagroup.streamplatform.streamregistry.core.views.ConsumerView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProducerView;
import com.expediagroup.streamplatform.streamregistry.core.views.SchemaView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamView;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;
import com.expediagroup.streamplatform.streamregistry.repository.ConsumerRepository;
import com.expediagroup.streamplatform.streamregistry.repository.ProducerRepository;
import com.expediagroup.streamplatform.streamregistry.repository.SchemaRepository;
import com.expediagroup.streamplatform.streamregistry.repository.StreamBindingRepository;
import com.expediagroup.streamplatform.streamregistry.repository.StreamRepository;

@RunWith(MockitoJUnitRunner.class)
public class StreamServiceTest {

  @Mock
  private HandlerService handlerService;

  @Mock
  private StreamValidator streamValidator;

  @Mock
  private StreamRepository streamRepository;

  @Mock
  private ConsumerService consumerService;

  @Mock
  private ProducerService producerService;

  @Mock
  private SchemaService schemaService;

  @Mock
  private StreamBindingService streamBindingService;

  @Mock
  private StreamBindingRepository streamBindingRepository;

  @Mock
  private ConsumerRepository consumerRepository;

  @Mock
  private ProducerRepository producerRepository;

  @Mock
  private SchemaRepository schemaRepository;

  private StreamService streamService;

  @Before
  public void before() {

    streamService = new StreamService(
      handlerService,
      streamValidator,
      streamRepository,
      streamBindingService,
      producerService,
      consumerService,
      schemaService,
      new StreamView(streamRepository),
      new StreamBindingView(streamBindingRepository),
      new ProducerView(producerRepository),
      new ConsumerView(consumerRepository),
      new SchemaView(schemaRepository)
    );
  }

  @Test
  public void create() {
    final StreamKey key = mock(StreamKey.class);
    final Specification specification = mock(Specification.class);

    final Stream entity = mock(Stream.class);
    when(entity.getKey()).thenReturn(key);
    when(streamRepository.findById(key)).thenReturn(Optional.empty());

    doNothing().when(streamValidator).validateForCreate(entity);
    when(handlerService.handleInsert(entity)).thenReturn(specification);

    when(streamRepository.save(entity)).thenReturn(entity);

    streamService.create(entity);

    verify(entity).getKey();
    verify(streamRepository).findById(key);
    verify(streamValidator).validateForCreate(entity);
    verify(handlerService).handleInsert(entity);
    verify(streamRepository).save(entity);
  }

  @Test
  public void update() {
    final StreamKey key = mock(StreamKey.class);
    final Specification specification = mock(Specification.class);

    final Stream entity = mock(Stream.class);
    final Stream existingEntity = mock(Stream.class);

    when(entity.getKey()).thenReturn(key);

    when(streamRepository.findById(key)).thenReturn(Optional.of(existingEntity));
    doNothing().when(streamValidator).validateForUpdate(entity, existingEntity);
    when(handlerService.handleUpdate(entity, existingEntity)).thenReturn(specification);

    when(streamRepository.save(entity)).thenReturn(entity);

    streamService.update(entity);

    verify(entity).getKey();
    verify(streamRepository).findById(key);
    verify(streamValidator).validateForUpdate(entity, existingEntity);
    verify(handlerService).handleUpdate(entity, existingEntity);
    verify(streamRepository).save(entity);
  }

  @Test
  public void updateStatus() {
    final Status status = mock(Status.class);
    final Stream entity = mock(Stream.class);

    when(streamRepository.save(entity)).thenReturn(entity);

    streamService.updateStatus(entity, status);

    verify(streamRepository).save(entity);
  }

  @Test
  public void delete() {
    final Stream entity = mock(Stream.class);
    streamService.delete(entity);
    verify(streamRepository).delete(entity);
  }
}
