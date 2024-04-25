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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.InfrastructureValidator;
import com.expediagroup.streamplatform.streamregistry.core.views.ConsumerBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.InfrastructureView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProducerBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamBindingView;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.InfrastructureKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamBindingKey;
import com.expediagroup.streamplatform.streamregistry.repository.ConsumerBindingRepository;
import com.expediagroup.streamplatform.streamregistry.repository.InfrastructureRepository;
import com.expediagroup.streamplatform.streamregistry.repository.ProducerBindingRepository;
import com.expediagroup.streamplatform.streamregistry.repository.StreamBindingRepository;

@RunWith(MockitoJUnitRunner.class)
public class InfrastructureServiceTest {

  @Mock
  private HandlerService handlerService;

  @Mock
  private InfrastructureValidator infrastructureValidator;

  @Mock
  private InfrastructureRepository infrastructureRepository;

  @Mock
  private StreamBindingRepository streamBindingRepository;

  @Mock
  private ConsumerBindingRepository consumerBindingRepository;

  @Mock
  private ProducerBindingRepository producerBindingRepository;

  private InfrastructureService infrastructureService;

  @Before
  public void before() {
    infrastructureService = new InfrastructureService(
      new InfrastructureView(infrastructureRepository),
      handlerService,
      infrastructureValidator,
      infrastructureRepository,
      new StreamBindingView(streamBindingRepository),
      new ConsumerBindingView(consumerBindingRepository),
      new ProducerBindingView(producerBindingRepository)
    );
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
    final Status status = mock(Status.class);

    when(infrastructureRepository.save(entity)).thenReturn(entity);

    infrastructureService.updateStatus(entity, status);

    verify(infrastructureRepository).save(entity);
  }

  @Test
  public void deleteWithNoError() {
    final Infrastructure infrastructure = mock(Infrastructure.class);
    when(streamBindingRepository.findAll()).thenReturn(emptyList());
    when(consumerBindingRepository.findAll()).thenReturn(emptyList());
    when(producerBindingRepository.findAll()).thenReturn(emptyList());
    infrastructureService.delete(infrastructure);
    verify(infrastructureRepository).delete(infrastructure);
  }

  @Test
  public void throwExceptionWhenInfrastructureIsUsedInStreamBinding() {
    final InfrastructureKey infrastructureKey = new InfrastructureKey("aws_us_east_1", "kafka-1c");
    final Infrastructure infrastructure = mock(Infrastructure.class);

    final StreamBindingKey streamBindingKey = new StreamBindingKey(
      "domain",
      "stream",
      1,
      "aws_us_east_1",
      "kafka-1c"
    );

    final StreamBinding streamBinding = mock(StreamBinding.class);

    when(streamBinding.getKey()).thenReturn(streamBindingKey);
    when(infrastructure.getKey()).thenReturn(infrastructureKey);
    when(streamBindingRepository.findAll()).thenReturn(Collections.singletonList(streamBinding));

    IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> infrastructureService.delete(infrastructure));
    Assertions.assertEquals("Infrastructure is used in stream: StreamKey(domain=domain, name=stream, version=1)", ex.getMessage());
  }

  @Test
  public void throwExceptionWhenZoneIsUsedInConsumerBinding() {
    final InfrastructureKey infrastructureKey = new InfrastructureKey("aws_us_east_1", "kafka-1c");
    final Infrastructure infrastructure = mock(Infrastructure.class);

    final ConsumerBindingKey consumerBindingKey = new ConsumerBindingKey(
      "domain",
      "stream",
      1,
      "aws_us_east_1",
      "kafka-1c",
      "consumer"
    );

    final ConsumerBinding consumerBinding = mock(ConsumerBinding.class);

    when(streamBindingRepository.findAll()).thenReturn(emptyList());
    when(consumerBinding.getKey()).thenReturn(consumerBindingKey);
    when(infrastructure.getKey()).thenReturn(infrastructureKey);
    when(consumerBindingRepository.findAll()).thenReturn(Collections.singletonList(consumerBinding));

    IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> infrastructureService.delete(infrastructure));
    Assertions.assertEquals("Infrastructure is used in consumer binding: " + consumerBindingKey, ex.getMessage());
  }

  @Test
  public void throwExceptionWhenZoneIsUsedInProducerBinding() {
    final InfrastructureKey infrastructureKey = new InfrastructureKey("aws_us_east_1", "kafka-1c");
    final Infrastructure infrastructure = mock(Infrastructure.class);

    final ProducerBindingKey producerBindingKey = new ProducerBindingKey(
      "domain",
      "stream",
      1,
      "aws_us_east_1",
      "kafka-1c",
      "producer"
    );

    final ProducerBinding producerBinding = mock(ProducerBinding.class);

    when(streamBindingRepository.findAll()).thenReturn(emptyList());
    when(consumerBindingRepository.findAll()).thenReturn(emptyList());
    when(producerBinding.getKey()).thenReturn(producerBindingKey);
    when(infrastructure.getKey()).thenReturn(infrastructureKey);
    when(producerBindingRepository.findAll()).thenReturn(Collections.singletonList(producerBinding));

    IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> infrastructureService.delete(infrastructure));
    Assertions.assertEquals("Infrastructure is used in producer binding: " + producerBindingKey, ex.getMessage());
  }
}
