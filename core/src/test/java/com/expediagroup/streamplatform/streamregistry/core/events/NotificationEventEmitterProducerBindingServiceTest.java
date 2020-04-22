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

import static com.expediagroup.streamplatform.streamregistry.data.ObjectNodeMapper.deserialise;
import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.test.context.junit4.SpringRunner;

import com.expediagroup.streamplatform.streamregistry.DataToModel;
import com.expediagroup.streamplatform.streamregistry.ModelToData;
import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.repositories.ProducerBindingRepository;
import com.expediagroup.streamplatform.streamregistry.core.services.ProducerBindingService;
import com.expediagroup.streamplatform.streamregistry.core.validators.ProducerBindingValidator;
import com.expediagroup.streamplatform.streamregistry.data.ProducerBindingData;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Specification;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfig.class)
public class NotificationEventEmitterProducerBindingServiceTest {

  private DataToModel dataToModel = new DataToModel();
  private ModelToData modelToData = new ModelToData();

  @MockBean
  private ApplicationEventMulticaster applicationEventMulticaster;

  @MockBean
  private HandlerService handlerService;

  @MockBean
  private ProducerBindingValidator producerBindingValidator;

  @MockBean
  private ProducerBindingRepository producerBindingRepository;

  private NotificationEventEmitter<ProducerBinding> producerBindingServiceEventEmitter;
  private ProducerBindingService producerBindingService;

  @Before
  public void before() {
    producerBindingServiceEventEmitter = Mockito.spy(DefaultNotificationEventEmitter.<ProducerBinding> builder()
        .classType(ProducerBinding.class)
        .applicationEventMulticaster(applicationEventMulticaster)
        .build());

    producerBindingService = Mockito.spy(
        new ProducerBindingService(dataToModel, modelToData, handlerService, producerBindingValidator, producerBindingRepository,
            producerBindingServiceEventEmitter));
  }

  @Test
  public void givenAProducerBindingForCreate_validateThatNotificationEventIsEmitted() {
    final ProducerBinding entity = new ProducerBinding();
    final ProducerBindingData data = modelToData.convertToData(entity);

    final EventType type = EventType.CREATE;
    final String source = producerBindingServiceEventEmitter.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
    final NotificationEvent<ProducerBinding> event = getDummyNotificationEvent(source, type, entity);

    Mockito.when(producerBindingRepository.findById(any())).thenReturn(Optional.empty());
    Mockito.doNothing().when(producerBindingValidator).validateForCreate(entity);
    Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());
    Mockito.when(producerBindingRepository.save(any())).thenReturn(data);
    Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

    producerBindingService.create(entity);

    Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
        .multicastEvent(event);

    Mockito.verify(producerBindingServiceEventEmitter, Mockito.timeout(1000).times(0))
        .onFailedEmitting(any(), Mockito.eq(event));
  }

  @Test
  public void givenAProducerBindingForUpdate_validateThatNotificationEventIsEmitted() {
    final ProducerBinding entity = new ProducerBinding();
    final ProducerBindingData data = modelToData.convertToData(entity);

    final EventType type = EventType.UPDATE;
    final String source = producerBindingServiceEventEmitter.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
    final NotificationEvent<ProducerBinding> event = getDummyNotificationEvent(source, type, entity);

    Mockito.when(producerBindingRepository.findById(any())).thenReturn(Optional.of(data));
    Mockito.doNothing().when(producerBindingValidator).validateForUpdate(Mockito.eq(entity), any());
    Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), any())).thenReturn(getDummySpecification());

    Mockito.when(producerBindingRepository.save(any())).thenReturn(data);
    Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

    producerBindingService.update(entity);

    Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
        .multicastEvent(event);

    Mockito.verify(producerBindingServiceEventEmitter, Mockito.timeout(1000).times(0))
        .onFailedEmitting(any(), Mockito.eq(event));
  }

  @Test
  public void givenANullProducerBindingRetrievedByRepositoryForCreate_validateThatNotificationEventIsNotEmitted() {
    final ProducerBinding entity = new ProducerBinding();
    final ProducerBindingData data = modelToData.convertToData(entity);

    Mockito.when(producerBindingRepository.findById(any())).thenReturn(Optional.empty());
    Mockito.doNothing().when(producerBindingValidator).validateForCreate(entity);
    Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());

    Mockito.when(producerBindingRepository.save(any())).thenReturn(null);
    Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(any());

    producerBindingService.create(entity);

    Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(0))
        .multicastEvent(any());

    Mockito.verify(producerBindingServiceEventEmitter, Mockito.timeout(1000).times(0))
        .onFailedEmitting(any(), any());
  }

  @Test
  public void givenANullProducerBindingRetrievedByRepositoryForUpdate_validateThatNotificationEventIsNotEmitted() {
    final ProducerBinding entity = new ProducerBinding();
    final ProducerBindingData data = modelToData.convertToData(entity);

    Mockito.when(producerBindingRepository.findById(any())).thenReturn(Optional.of(data));
    Mockito.doNothing().when(producerBindingValidator).validateForUpdate(Mockito.eq(entity), any());
    Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), any())).thenReturn(getDummySpecification());

    Mockito.when(producerBindingRepository.save(any())).thenReturn(null);
    Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(any());

    producerBindingService.update(entity);

    Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(0))
        .multicastEvent(any());

    Mockito.verify(producerBindingServiceEventEmitter, Mockito.timeout(1000).times(0))
        .onFailedEmitting(any(), any());
  }

  @Test
  public void givenAProducerBindingForUpsert_validateThatNotificationEventIsEmitted() {
    final ProducerBinding entity = new ProducerBinding();
    final ProducerBindingData data = modelToData.convertToData(entity);

    final EventType type = EventType.UPDATE;
    final String source = producerBindingServiceEventEmitter.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
    final NotificationEvent<ProducerBinding> event = getDummyNotificationEvent(source, type, entity);

    Mockito.when(producerBindingRepository.findById(any())).thenReturn(Optional.of(data));
    Mockito.doNothing().when(producerBindingValidator).validateForUpdate(Mockito.eq(entity), any());
    Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), any())).thenReturn(getDummySpecification());

    Mockito.when(producerBindingRepository.save(any())).thenReturn(data);
    Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

    producerBindingService.upsert(entity);

    Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
        .multicastEvent(event);

    Mockito.verify(producerBindingServiceEventEmitter, Mockito.timeout(1000).times(0))
        .onFailedEmitting(any(), Mockito.eq(event));
  }

  @Test
  public void givenAProducerBindingForCreate_handleAMulticasterException() {
    final ProducerBinding entity = new ProducerBinding();
    final ProducerBindingData data = modelToData.convertToData(entity);

    final EventType type = EventType.CREATE;
    final String source = producerBindingServiceEventEmitter.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
    final NotificationEvent<ProducerBinding> event = getDummyNotificationEvent(source, type, entity);

    Mockito.when(producerBindingRepository.findById(any())).thenReturn(Optional.empty());
    Mockito.doNothing().when(producerBindingValidator).validateForCreate(entity);
    Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());

    Mockito.when(producerBindingRepository.save(any())).thenReturn(data);
    Mockito.doThrow(new RuntimeException("BOOOOOOOM")).when(applicationEventMulticaster).multicastEvent(event);

    Optional<ProducerBinding> response = producerBindingService.create(entity);

    Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
        .multicastEvent(event);

    Mockito.verify(producerBindingServiceEventEmitter, Mockito.timeout(1000).times(1))
        .onFailedEmitting(any(), Mockito.eq(event));

    Assert.assertTrue(response.isPresent());
    Assert.assertEquals(response.get(), entity);
  }

  public <T> NotificationEvent<T> getDummyNotificationEvent(String source, EventType type, T entity) {
    return NotificationEvent.<T> builder()
        .source(source)
        .eventType(type)
        .entity(entity)
        .build();
  }

  public Specification getDummySpecification() {
    Specification spec = new Specification();
    spec.setConfiguration(deserialise("{}"));
    spec.setDescription("dummy spec");
    return spec;
  }
}