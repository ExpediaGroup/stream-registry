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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.ProcessBindingValidator;
import com.expediagroup.streamplatform.streamregistry.core.views.ConsumerView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProcessBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProducerView;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.ProcessBinding;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProcessBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.repository.ConsumerRepository;
import com.expediagroup.streamplatform.streamregistry.repository.ProcessBindingRepository;
import com.expediagroup.streamplatform.streamregistry.repository.ProducerRepository;

@RunWith(MockitoJUnitRunner.class)
public class ProcessBindingServiceTest {

  @Mock
  private HandlerService handlerService;

  @Mock
  private ProcessBindingValidator processBindingValidator;

  @Mock
  private ProcessBindingRepository processBindingRepository;

  @Mock
  private ConsumerService consumerService;

  @Mock
  private ProducerService producerService;

  @Mock
  private ConsumerRepository consumerRepository;

  @Mock
  private ProducerRepository producerRepository;

  private ProcessBindingService processBindingService;

  @Before
  public void before() {
    processBindingService = new ProcessBindingService(
      new ProcessBindingView(processBindingRepository),
      handlerService,
      processBindingValidator,
      processBindingRepository,
      new ConsumerView(consumerRepository),
      consumerService,
      new ProducerView(producerRepository),
      producerService
    );
  }

  @Test
  public void create() {
    final ProcessBindingKey key = mock(ProcessBindingKey.class);
    final Specification specification = mock(Specification.class);

    final ProcessBinding entity = mock(ProcessBinding.class);
    when(entity.getKey()).thenReturn(key);
    when(processBindingRepository.findById(key)).thenReturn(Optional.empty());

    doNothing().when(processBindingValidator).validateForCreate(entity);
    when(handlerService.handleInsert(entity)).thenReturn(specification);

    when(processBindingRepository.save(entity)).thenReturn(entity);

    processBindingService.create(entity);

    verify(entity).getKey();
    verify(processBindingRepository).findById(key);
    verify(processBindingValidator).validateForCreate(entity);
    verify(handlerService).handleInsert(entity);
    verify(processBindingRepository).save(entity);
  }

  @Test
  public void update() {
    final ProcessBindingKey key = mock(ProcessBindingKey.class);
    final Specification specification = mock(Specification.class);

    final ProcessBinding entity = mock(ProcessBinding.class);
    final ProcessBinding existingEntity = mock(ProcessBinding.class);

    when(entity.getKey()).thenReturn(key);

    when(processBindingRepository.findById(key)).thenReturn(Optional.of(existingEntity));
    doNothing().when(processBindingValidator).validateForUpdate(entity, existingEntity);
    when(handlerService.handleUpdate(entity, existingEntity)).thenReturn(specification);

    when(processBindingRepository.save(entity)).thenReturn(entity);

    processBindingService.update(entity);

    verify(entity).getKey();
    verify(processBindingRepository).findById(key);
    verify(processBindingValidator).validateForUpdate(entity, existingEntity);
    verify(handlerService).handleUpdate(entity, existingEntity);
    verify(processBindingRepository).save(entity);
  }

  @Test
  public void updateStatus() {
    final Status status = mock(Status.class);
    final ProcessBinding entity = mock(ProcessBinding.class);

    when(processBindingRepository.save(entity)).thenReturn(entity);

    processBindingService.updateStatus(entity, status);

    verify(processBindingRepository).save(entity);
  }

  @Test
  public void delete_multipleEntities() {
    final ProcessBinding entity = mock(ProcessBinding.class);

    final ConsumerKey consumerKey = mock(ConsumerKey.class);
    final Consumer consumer = mock(Consumer.class);
    final ConsumerBindingKey consumerBindingKey = mock(ConsumerBindingKey.class);
    when(consumer.getKey()).thenReturn(consumerKey);
    when(consumerBindingKey.getConsumerKey()).thenReturn(consumerKey);

    final ConsumerKey consumerKey2 = mock(ConsumerKey.class);
    final Consumer consumer2 = mock(Consumer.class);
    final ConsumerBindingKey consumerBindingKey2 = mock(ConsumerBindingKey.class);
    when(consumer2.getKey()).thenReturn(consumerKey2);
    when(consumerBindingKey2.getConsumerKey()).thenReturn(consumerKey2);

    final ProducerKey producerKey = mock(ProducerKey.class);
    final Producer producer = mock(Producer.class);
    final ProducerBindingKey producerBindingKey = mock(ProducerBindingKey.class);
    when(producer.getKey()).thenReturn(producerKey);
    when(producerBindingKey.getProducerKey()).thenReturn(producerKey);

    final ProducerKey producerKey2 = mock(ProducerKey.class);
    final Producer producer2 = mock(Producer.class);
    final ProducerBindingKey producerBindingKey2 = mock(ProducerBindingKey.class);
    when(producer2.getKey()).thenReturn(producerKey2);
    when(producerBindingKey2.getProducerKey()).thenReturn(producerKey2);

    when(entity.getInputs()).thenReturn(Arrays.asList(consumerBindingKey, consumerBindingKey2));
    when(entity.getOutputs()).thenReturn(Arrays.asList(producerBindingKey, producerBindingKey2));
    when(consumerRepository.findAll()).thenReturn(Arrays.asList(consumer, consumer2));
    when(producerRepository.findAll()).thenReturn(Arrays.asList(producer, producer2));

    processBindingService.delete(entity);

    InOrder inOrder = inOrder(handlerService, producerService, consumerService, processBindingRepository);
    inOrder.verify(handlerService).handleDelete(entity);
    inOrder.verify(consumerService).delete(consumer);
    inOrder.verify(consumerService).delete(consumer2);
    inOrder.verify(producerService).delete(producer);
    inOrder.verify(producerService).delete(producer2);
    inOrder.verify(processBindingRepository).delete(entity);

  }
}
