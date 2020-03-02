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

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.repositories.ProducerRepository;
import com.expediagroup.streamplatform.streamregistry.core.services.ProducerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.ProducerValidator;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.Specification;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfig.class)
public class NotificationEventEmitterProducerServiceTest {

  @MockBean
  private ApplicationEventMulticaster applicationEventMulticaster;

  @MockBean
  private HandlerService handlerService;

  @MockBean
  private ProducerValidator producerValidator;

  @MockBean
  private ProducerRepository producerRepository;

  private NotificationEventEmitter<Producer> producerServiceEventEmitter;
  private ProducerService producerService;

  @Before
  public void before() {
    producerServiceEventEmitter = Mockito.spy(DefaultNotificationEventEmitter.<Producer>builder()
        .classType(Producer.class)
        .applicationEventMulticaster(applicationEventMulticaster)
        .build());

    producerService = Mockito.spy(new ProducerService(handlerService, producerValidator, producerRepository, producerServiceEventEmitter));
  }

  @Test
  public void givenAProducerForCreate_validateThatNotificationEventIsEmitted() {
    final Producer entity = getDummyProducer();
    final EventType type = EventType.CREATE;
    final String source = producerServiceEventEmitter.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
    final NotificationEvent<Producer> event = getDummyNotificationEvent(source, type, entity);

    Mockito.when(producerRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    Mockito.doNothing().when(producerValidator).validateForCreate(entity);
    Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());
    Mockito.when(producerRepository.save(entity)).thenReturn(entity);
    Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

    producerService.create(entity);

    Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
        .multicastEvent(event);

    Mockito.verify(producerServiceEventEmitter, Mockito.timeout(1000).times(0))
        .onFailedEmitting(Mockito.any(), Mockito.eq(event));
  }

  @Test
  public void givenAProducerForUpdate_validateThatNotificationEventIsEmitted() {
    final Producer entity = getDummyProducer();
    final EventType type = EventType.UPDATE;
    final String source = producerServiceEventEmitter.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
    final NotificationEvent<Producer> event = getDummyNotificationEvent(source, type, entity);

    Mockito.when(producerRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
    Mockito.doNothing().when(producerValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
    Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

    Mockito.when(producerRepository.save(entity)).thenReturn(entity);
    Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

    producerService.update(entity);

    Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
        .multicastEvent(event);

    Mockito.verify(producerServiceEventEmitter, Mockito.timeout(1000).times(0))
        .onFailedEmitting(Mockito.any(), Mockito.eq(event));
  }

  @Test
  public void givenANullProducerRetrievedByRepositoryForCreate_validateThatNotificationEventIsNotEmitted() {
    final Producer entity = getDummyProducer();

    Mockito.when(producerRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    Mockito.doNothing().when(producerValidator).validateForCreate(entity);
    Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());

    Mockito.when(producerRepository.save(entity)).thenReturn(null);
    Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(Mockito.any());

    producerService.create(entity);

    Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(0))
        .multicastEvent(Mockito.any());

    Mockito.verify(producerServiceEventEmitter, Mockito.timeout(1000).times(0))
        .onFailedEmitting(Mockito.any(), Mockito.any());
  }

  @Test
  public void givenANullProducerRetrievedByRepositoryForUpdate_validateThatNotificationEventIsNotEmitted() {
    final Producer entity = getDummyProducer();

    Mockito.when(producerRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
    Mockito.doNothing().when(producerValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
    Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

    Mockito.when(producerRepository.save(entity)).thenReturn(null);
    Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(Mockito.any());

    producerService.update(entity);

    Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(0))
        .multicastEvent(Mockito.any());

    Mockito.verify(producerServiceEventEmitter, Mockito.timeout(1000).times(0))
        .onFailedEmitting(Mockito.any(), Mockito.any());
  }

  @Test
  public void givenAProducerForUpsert_validateThatNotificationEventIsEmitted() {
    final Producer entity = getDummyProducer();
    final EventType type = EventType.UPDATE;
    final String source = producerServiceEventEmitter.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
    final NotificationEvent<Producer> event = getDummyNotificationEvent(source, type, entity);

    Mockito.when(producerRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
    Mockito.doNothing().when(producerValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
    Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

    Mockito.when(producerRepository.save(entity)).thenReturn(entity);
    Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

    producerService.upsert(entity);

    Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
        .multicastEvent(event);

    Mockito.verify(producerServiceEventEmitter, Mockito.timeout(1000).times(0))
        .onFailedEmitting(Mockito.any(), Mockito.eq(event));
  }

  @Test
  public void givenAProducerForCreate_handleAMulticasterException() {
    final Producer entity = getDummyProducer();
    final EventType type = EventType.CREATE;
    final String source = producerServiceEventEmitter.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
    final NotificationEvent<Producer> event = getDummyNotificationEvent(source, type, entity);

    Mockito.when(producerRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    Mockito.doNothing().when(producerValidator).validateForCreate(entity);
    Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());

    Mockito.when(producerRepository.save(entity)).thenReturn(entity);
    Mockito.doThrow(new RuntimeException("BOOOOOOOM")).when(applicationEventMulticaster).multicastEvent(event);

    Optional<Producer> response = producerService.create(entity);

    Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
        .multicastEvent(event);

    Mockito.verify(producerServiceEventEmitter, Mockito.timeout(1000).times(1))
        .onFailedEmitting(Mockito.any(), Mockito.eq(event));

    Assert.assertTrue(response.isPresent());
    Assert.assertEquals(response.get(), entity);
  }

  public <T> NotificationEvent<T> getDummyNotificationEvent(String source, EventType type, T entity) {
    return NotificationEvent.<T>builder()
        .source(source)
        .eventType(type)
        .entity(entity)
        .build();
  }

  public Producer getDummyProducer() {
    final Producer entity = new Producer();

    return entity;
  }

  public Specification getDummySpecification() {
    Specification spec = new Specification();
    spec.setConfigJson("{}");
    spec.setDescription("dummy spec");

    return spec;
  }
}