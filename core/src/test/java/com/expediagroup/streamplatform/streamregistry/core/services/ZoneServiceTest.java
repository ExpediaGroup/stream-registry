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

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.ZoneValidator;
import com.expediagroup.streamplatform.streamregistry.core.views.ConsumerBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.InfrastructureView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProcessBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProcessView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProducerBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.ZoneView;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.model.Process;
import com.expediagroup.streamplatform.streamregistry.model.ProcessBinding;
import com.expediagroup.streamplatform.streamregistry.model.ProcessInputStreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.ProcessOutputStreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.Zone;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.InfrastructureKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProcessBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;
import com.expediagroup.streamplatform.streamregistry.repository.ConsumerBindingRepository;
import com.expediagroup.streamplatform.streamregistry.repository.InfrastructureRepository;
import com.expediagroup.streamplatform.streamregistry.repository.ProcessBindingRepository;
import com.expediagroup.streamplatform.streamregistry.repository.ProcessRepository;
import com.expediagroup.streamplatform.streamregistry.repository.ProducerBindingRepository;
import com.expediagroup.streamplatform.streamregistry.repository.StreamBindingRepository;
import com.expediagroup.streamplatform.streamregistry.repository.ZoneRepository;

@RunWith(MockitoJUnitRunner.class)
public class ZoneServiceTest {

  @Mock
  private HandlerService handlerService;

  @Mock
  private ZoneValidator zoneValidator;

  @Mock
  private ZoneRepository zoneRepository;

  @Mock
  private StreamBindingRepository streamBindingRepository;

  @Mock
  private ConsumerBindingRepository consumerBindingRepository;

  @Mock
  private ProducerBindingRepository producerBindingRepository;

  @Mock
  private ProcessBindingRepository processBindingRepository;

  @Mock
  private ProcessRepository processRepository;

  @Mock
  private InfrastructureRepository infrastructureRepository;

  private ZoneService zoneService;

  @Before
  public void before() {
    zoneService = new ZoneService(
      handlerService,
      zoneValidator,
      zoneRepository,
      new ZoneView(zoneRepository),
      new StreamBindingView(streamBindingRepository),
      new ConsumerBindingView(consumerBindingRepository),
      new ProducerBindingView(producerBindingRepository),
      new ProcessBindingView(processBindingRepository),
      new ProcessView(processRepository),
      new InfrastructureView(infrastructureRepository)
    );
  }

  @Test
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

  @Test
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

  @Test
  public void updateStatus() {
    final Zone entity = mock(Zone.class);
    final Status status = mock(Status.class);

    when(zoneRepository.save(entity)).thenReturn(entity);

    zoneService.updateStatus(entity, status);

    verify(zoneRepository).save(entity);
  }

  @Test
  public void deleteWithNoError() {
    final Zone zone = mock(Zone.class);
    when(streamBindingRepository.findAll()).thenReturn(emptyList());
    when(consumerBindingRepository.findAll()).thenReturn(emptyList());
    when(producerBindingRepository.findAll()).thenReturn(emptyList());
    when(processBindingRepository.findAll()).thenReturn(emptyList());
    when(processRepository.findAll()).thenReturn(emptyList());
    when(infrastructureRepository.findAll()).thenReturn(emptyList());
    zoneService.delete(zone);
    verify(zoneRepository).delete(zone);
  }

  @Test
  public void deletionShouldThrowExceptionWhenZoneIsUsedInStreamBinding() {
    final ZoneKey zoneKey = new ZoneKey("aws_us_east_1");
    final Zone zone = mock(Zone.class);

    final StreamBindingKey streamBindingKey = new StreamBindingKey(
      "domain",
      "stream",
      1,
      "aws_us_east_1",
      "infra"
    );

    final StreamBinding streamBinding = mock(StreamBinding.class);

    when(streamBinding.getKey()).thenReturn(streamBindingKey);
    when(zone.getKey()).thenReturn(zoneKey);
    when(streamBindingRepository.findAll()).thenReturn(Collections.singletonList(streamBinding));

    IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> zoneService.delete(zone));
    Assertions.assertEquals("Zone is used in stream: StreamKey(domain=domain, name=stream, version=1)", ex.getMessage());
  }

  @Test
  public void deletionShouldThrowExceptionWhenZoneIsUsedInConsumerBinding() {
    final ZoneKey zoneKey = new ZoneKey("aws_us_east_1");
    final Zone zone = mock(Zone.class);

    final ConsumerBindingKey consumerBindingKey = new ConsumerBindingKey(
      "domain",
      "stream",
      1,
      "aws_us_east_1",
      "infra",
      "consumer"
    );

    final ConsumerBinding consumerBinding = mock(ConsumerBinding.class);

    when(streamBindingRepository.findAll()).thenReturn(emptyList());
    when(consumerBinding.getKey()).thenReturn(consumerBindingKey);
    when(zone.getKey()).thenReturn(zoneKey);
    when(consumerBindingRepository.findAll()).thenReturn(Collections.singletonList(consumerBinding));

    IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> zoneService.delete(zone));
    Assertions.assertEquals("Zone is used in consumer binding: " + consumerBindingKey, ex.getMessage());
  }

  @Test
  public void deletionShouldThrowExceptionWhenZoneIsUsedInProducerBinding() {
    final ZoneKey zoneKey = new ZoneKey("aws_us_east_1");
    final Zone zone = mock(Zone.class);

    final ProducerBindingKey producerBindingKey = new ProducerBindingKey(
      "domain",
      "stream",
      1,
      "aws_us_east_1",
      "infra",
      "producer"
    );

    final ProducerBinding producerBinding = mock(ProducerBinding.class);

    when(streamBindingRepository.findAll()).thenReturn(emptyList());
    when(consumerBindingRepository.findAll()).thenReturn(emptyList());
    when(producerBinding.getKey()).thenReturn(producerBindingKey);
    when(zone.getKey()).thenReturn(zoneKey);
    when(producerBindingRepository.findAll()).thenReturn(Collections.singletonList(producerBinding));

    IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> zoneService.delete(zone));
    Assertions.assertEquals("Zone is used in producer binding: " + producerBindingKey, ex.getMessage());
  }

  @Test
  public void deletionShouldThrowExceptionWhenZoneIsUsedInProcessBinding() {
    final ZoneKey zoneKey = new ZoneKey("aws_us_east_1");
    final Zone zone = mock(Zone.class);

    final ProcessBindingKey processBindingKey = new ProcessBindingKey(
      "domain",
      "aws_us_east_1",
      "process"
    );

    final ProcessBinding processBinding = mock(ProcessBinding.class);

    when(streamBindingRepository.findAll()).thenReturn(emptyList());
    when(consumerBindingRepository.findAll()).thenReturn(emptyList());
    when(producerBindingRepository.findAll()).thenReturn(emptyList());
    when(processBinding.getKey()).thenReturn(processBindingKey);
    when(zone.getKey()).thenReturn(zoneKey);
    when(processBindingRepository.findAll()).thenReturn(Collections.singletonList(processBinding));

    IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> zoneService.delete(zone));
    Assertions.assertEquals("Zone is used in process binding: " + processBindingKey, ex.getMessage());
  }

  @Test
  public void deletionShouldThrowExceptionWhenZoneIsUsedInProcessBindingOutput() {
    final ZoneKey zoneKey = new ZoneKey("aws_us_east_1");
    final Zone zone = mock(Zone.class);
    final ProcessBindingKey processBindingKey = new ProcessBindingKey(
      "domain",
      "aws_us_east_2",
      "process"
    );
    final ProcessBinding processBinding = mock(ProcessBinding.class);

    final ProcessOutputStreamBinding processOutputStreamBinding = new ProcessOutputStreamBinding(
      new StreamBindingKey("domain", "stream", 1, "aws_us_east_1", "kafka-1c"),
      new ObjectMapper().createObjectNode()
    );

    when(streamBindingRepository.findAll()).thenReturn(emptyList());
    when(consumerBindingRepository.findAll()).thenReturn(emptyList());
    when(producerBindingRepository.findAll()).thenReturn(emptyList());
    when(processBindingRepository.findAll()).thenReturn(Collections.singletonList(processBinding));
    when(processBinding.getOutputs()).thenReturn(Collections.singletonList(processOutputStreamBinding));
    when(processBinding.getKey()).thenReturn(processBindingKey);
    when(zone.getKey()).thenReturn(zoneKey);

    IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> zoneService.delete(zone));
    Assertions.assertEquals("Zone is used in process binding: " + processBinding.getKey(), ex.getMessage());
  }

  @Test
  public void deletionShouldThrowExceptionWhenZoneIsUsedInProcessBindingInput() {
    final ZoneKey zoneKey = new ZoneKey("aws_us_east_1");
    final Zone zone = mock(Zone.class);
    final ProcessBindingKey processBindingKey = new ProcessBindingKey(
      "domain",
      "aws_us_east_2",
      "process"
    );
    final ProcessBinding processBinding = mock(ProcessBinding.class);

    final ProcessInputStreamBinding processInputStreamBinding = new ProcessInputStreamBinding(
      new StreamBindingKey("domain", "stream", 1, "aws_us_east_1", "kafka-1c"),
      new ObjectMapper().createObjectNode()
    );

    when(streamBindingRepository.findAll()).thenReturn(emptyList());
    when(consumerBindingRepository.findAll()).thenReturn(emptyList());
    when(producerBindingRepository.findAll()).thenReturn(emptyList());
    when(processBindingRepository.findAll()).thenReturn(Collections.singletonList(processBinding));
    when(processBinding.getInputs()).thenReturn(Collections.singletonList(processInputStreamBinding));
    when(processBinding.getKey()).thenReturn(processBindingKey);
    when(zone.getKey()).thenReturn(zoneKey);

    IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> zoneService.delete(zone));
    Assertions.assertEquals("Zone is used in process binding: " + processBinding.getKey(), ex.getMessage());
  }

  @Test
  public void deletionShouldThrowExceptionWhenZoneIsUsedInProcess() {
    final ZoneKey zoneKey = new ZoneKey("aws_us_east_1");
    final Zone zone = mock(Zone.class);
    final Process process = mock(Process.class);

    when(streamBindingRepository.findAll()).thenReturn(emptyList());
    when(consumerBindingRepository.findAll()).thenReturn(emptyList());
    when(producerBindingRepository.findAll()).thenReturn(emptyList());
    when(processBindingRepository.findAll()).thenReturn(emptyList());
    when(processRepository.findAll()).thenReturn(Collections.singletonList(process));
    when(process.getZones()).thenReturn(Collections.singletonList(zoneKey));
    when(zone.getKey()).thenReturn(zoneKey);

    IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> zoneService.delete(zone));
    Assertions.assertEquals("Zone is used in process: " + process.getKey(), ex.getMessage());
  }

  @Test
  public void deletionShouldThrowExceptionWhenZoneIsUsedInInfrastructure() {
    final ZoneKey zoneKey = new ZoneKey("aws_us_east_1");

    final Zone zone = mock(Zone.class);
    final Infrastructure infrastructure = mock(Infrastructure.class);
    final InfrastructureKey infrastructureKey = new InfrastructureKey("aws_us_east_1", "infrastructure");

    when(streamBindingRepository.findAll()).thenReturn(emptyList());
    when(consumerBindingRepository.findAll()).thenReturn(emptyList());
    when(producerBindingRepository.findAll()).thenReturn(emptyList());
    when(processBindingRepository.findAll()).thenReturn(emptyList());
    when(processRepository.findAll()).thenReturn(emptyList());
    when(infrastructureRepository.findAll()).thenReturn(Collections.singletonList(infrastructure));
    when(zone.getKey()).thenReturn(zoneKey);
    when(infrastructure.getKey()).thenReturn(infrastructureKey);

    IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> zoneService.delete(zone));
    Assertions.assertEquals("Zone is used in infrastructure: " + infrastructure.getKey(), ex.getMessage());
  }
}
