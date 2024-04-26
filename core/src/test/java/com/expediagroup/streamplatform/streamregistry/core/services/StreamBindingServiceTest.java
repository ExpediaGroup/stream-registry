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

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.StreamBindingValidator;
import com.expediagroup.streamplatform.streamregistry.core.views.ConsumerBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProcessBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProducerBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamBindingView;
import com.expediagroup.streamplatform.streamregistry.model.ProcessBinding;
import com.expediagroup.streamplatform.streamregistry.model.ProcessInputStreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.ProcessOutputStreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamBindingKey;
import com.expediagroup.streamplatform.streamregistry.repository.ConsumerBindingRepository;
import com.expediagroup.streamplatform.streamregistry.repository.ProcessBindingRepository;
import com.expediagroup.streamplatform.streamregistry.repository.ProducerBindingRepository;
import com.expediagroup.streamplatform.streamregistry.repository.StreamBindingRepository;

@RunWith(MockitoJUnitRunner.class)
public class StreamBindingServiceTest {

  @Mock
  private HandlerService handlerService;

  @Mock
  private StreamBindingValidator streamBindingValidator;

  @Mock
  private StreamBindingRepository streamBindingRepository;

  @Mock
  private ConsumerBindingService consumerBindingService;

  @Mock
  private ProducerBindingService producerBindingService;

  @Mock
  private ConsumerBindingRepository consumerBindingRepository;

  @Mock
  private ProducerBindingRepository producerBindingRepository;

  @Mock
  private ProcessBindingRepository processBindingRepository;

  private StreamBindingService streamBindingService;

  @Before
  public void before() {

    streamBindingService = new StreamBindingService(
      handlerService,
      streamBindingValidator,
      streamBindingRepository,
      consumerBindingService,
      producerBindingService,
      new ConsumerBindingView(consumerBindingRepository),
      new ProducerBindingView(producerBindingRepository),
      new StreamBindingView(streamBindingRepository),
      new ProcessBindingView(processBindingRepository)
    );
  }

  @Test
  public void create() {
    final StreamBindingKey key = mock(StreamBindingKey.class);
    final Specification specification = mock(Specification.class);

    final StreamBinding entity = mock(StreamBinding.class);
    when(entity.getKey()).thenReturn(key);
    when(streamBindingRepository.findById(key)).thenReturn(Optional.empty());

    doNothing().when(streamBindingValidator).validateForCreate(entity);
    when(handlerService.handleInsert(entity)).thenReturn(specification);

    when(streamBindingRepository.save(entity)).thenReturn(entity);

    streamBindingService.create(entity);

    verify(entity).getKey();
    verify(streamBindingRepository).findById(key);
    verify(streamBindingValidator).validateForCreate(entity);
    verify(handlerService).handleInsert(entity);
    verify(streamBindingRepository).save(entity);
  }

  @Test
  public void update() {
    final StreamBindingKey key = mock(StreamBindingKey.class);
    final Specification specification = mock(Specification.class);

    final StreamBinding entity = mock(StreamBinding.class);
    final StreamBinding existingEntity = mock(StreamBinding.class);

    when(entity.getKey()).thenReturn(key);

    when(streamBindingRepository.findById(key)).thenReturn(Optional.of(existingEntity));
    doNothing().when(streamBindingValidator).validateForUpdate(entity, existingEntity);
    when(handlerService.handleUpdate(entity, existingEntity)).thenReturn(specification);

    when(streamBindingRepository.save(entity)).thenReturn(entity);

    streamBindingService.update(entity);

    verify(entity).getKey();
    verify(streamBindingRepository).findById(key);
    verify(streamBindingValidator).validateForUpdate(entity, existingEntity);
    verify(handlerService).handleUpdate(entity, existingEntity);
    verify(streamBindingRepository).save(entity);
  }

  @Test
  public void updateStatus() {
    final Status status = mock(Status.class);
    final StreamBinding entity = mock(StreamBinding.class);

    when(streamBindingRepository.save(entity)).thenReturn(entity);

    streamBindingService.updateStatus(entity, status);

    verify(streamBindingRepository).save(entity);
  }

  @Test
  public void deleteWithNoError() {
    final StreamBinding entity = mock(StreamBinding.class);
    when(processBindingRepository.findAll()).thenReturn(emptyList());
    when(consumerBindingRepository.findAll()).thenReturn(emptyList());
    when(producerBindingRepository.findAll()).thenReturn(emptyList());
    streamBindingService.delete(entity);
    verify(streamBindingRepository).delete(entity);
  }

  @Test
  public void deletionShouldThrowExceptionWhenStreamBindingIsUsedInProcessBindingOutput() {
    final StreamBinding entity = mock(StreamBinding.class);
    final StreamBindingKey streamBindingKey = new StreamBindingKey(
      "domain",
      "stream",
      1,
      "aws_us_east_1",
      "kafka-1c"
    );
    final ProcessOutputStreamBinding processOutputStreamBinding = new ProcessOutputStreamBinding(
      streamBindingKey,
      new ObjectMapper().createObjectNode()
    );
    final ProcessBinding processBinding = mock(ProcessBinding.class);

    when(processBinding.getOutputs()).thenReturn(Collections.singletonList(processOutputStreamBinding));
    when(entity.getKey()).thenReturn(streamBindingKey);
    when(processBindingRepository.findAll()).thenReturn(Collections.singletonList(processBinding));

    IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> streamBindingService.delete(entity));
    Assertions.assertEquals("Stream binding is used in process binding: " + processBinding.getKey(), ex.getMessage());
  }

  @Test
  public void deletionShouldThrowExceptionWhenStreamBindingIsUsedInProcessBindingInput() {
    final StreamBinding entity = mock(StreamBinding.class);
    final StreamBindingKey streamBindingKey = new StreamBindingKey(
      "domain",
      "stream",
      1,
      "aws_us_east_1",
      "kafka-1c"
    );
    final ProcessInputStreamBinding processInputStreamBinding = new ProcessInputStreamBinding(
      streamBindingKey,
      new ObjectMapper().createObjectNode()
    );
    final ProcessBinding processBinding = mock(ProcessBinding.class);

    when(processBinding.getOutputs()).thenReturn(emptyList());
    when(processBinding.getInputs()).thenReturn(Collections.singletonList(processInputStreamBinding));
    when(entity.getKey()).thenReturn(streamBindingKey);
    when(processBindingRepository.findAll()).thenReturn(Collections.singletonList(processBinding));

    IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> streamBindingService.delete(entity));
    Assertions.assertEquals("Stream binding is used in process binding: " + processBinding.getKey(), ex.getMessage());
  }
}
