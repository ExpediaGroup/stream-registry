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
import com.expediagroup.streamplatform.streamregistry.core.repositories.ConsumerRepository;
import com.expediagroup.streamplatform.streamregistry.core.services.ConsumerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.ConsumerValidator;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.Specification;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfig.class)
public class NotificationEventEmitterConsumerServiceTest {

    @MockBean
    private ApplicationEventMulticaster applicationEventMulticaster;

    @MockBean
    private HandlerService handlerService;

    @MockBean
    private ConsumerValidator consumerValidator;

    @MockBean
    private ConsumerRepository consumerRepository;

    private NotificationEventEmitter<Consumer> consumerServiceEventEmitter;
    private ConsumerService consumerService;

    @Before
    public void before() {
        consumerServiceEventEmitter = Mockito.spy(DefaultNotificationEventEmitter.<Consumer>builder()
                .classType(Consumer.class)
                .applicationEventMulticaster(applicationEventMulticaster)
                .build());

        consumerService = Mockito.spy(new ConsumerService(handlerService, consumerValidator, consumerRepository, consumerServiceEventEmitter));
    }

    @Test
    public void givenAConsumerForCreate_validateThatNotificationEventIsEmitted() {
        final Consumer entity = getDummyConsumer();
        final EventType type = EventType.CREATE;
        final String source = consumerServiceEventEmitter.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<Consumer> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(consumerRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        Mockito.doNothing().when(consumerValidator).validateForCreate(entity);
        Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());
        Mockito.when(consumerRepository.save(entity)).thenReturn(entity);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

        consumerService.create(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(consumerServiceEventEmitter, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.eq(event));
    }

    @Test
    public void givenAConsumerForUpdate_validateThatNotificationEventIsEmitted() {
        final Consumer entity = getDummyConsumer();
        final EventType type = EventType.UPDATE;
        final String source = consumerServiceEventEmitter.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<Consumer> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(consumerRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
        Mockito.doNothing().when(consumerValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
        Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

        Mockito.when(consumerRepository.save(entity)).thenReturn(entity);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

        consumerService.update(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(consumerServiceEventEmitter, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.eq(event));
    }

    @Test
    public void givenANullConsumerRetrievedByRepositoryForCreate_validateThatNotificationEventIsNotEmitted() {
        final Consumer entity = getDummyConsumer();

        Mockito.when(consumerRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        Mockito.doNothing().when(consumerValidator).validateForCreate(entity);
        Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());

        Mockito.when(consumerRepository.save(entity)).thenReturn(null);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(Mockito.any());

        consumerService.create(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(0))
                .multicastEvent(Mockito.any());

        Mockito.verify(consumerServiceEventEmitter, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.any());
    }

    @Test
    public void givenANullConsumerRetrievedByRepositoryForUpdate_validateThatNotificationEventIsNotEmitted() {
        final Consumer entity = getDummyConsumer();

        Mockito.when(consumerRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
        Mockito.doNothing().when(consumerValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
        Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

        Mockito.when(consumerRepository.save(entity)).thenReturn(null);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(Mockito.any());

        consumerService.update(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(0))
                .multicastEvent(Mockito.any());

        Mockito.verify(consumerServiceEventEmitter, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.any());
    }

    @Test
    public void givenAConsumerForUpsert_validateThatNotificationEventIsEmitted() {
        final Consumer entity = getDummyConsumer();
        final EventType type = EventType.UPDATE;
        final String source = consumerServiceEventEmitter.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<Consumer> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(consumerRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
        Mockito.doNothing().when(consumerValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
        Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

        Mockito.when(consumerRepository.save(entity)).thenReturn(entity);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

        consumerService.upsert(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(consumerServiceEventEmitter, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.eq(event));
    }

    @Test
    public void givenAConsumerForCreate_handleAMulticasterException() {
        final Consumer entity = getDummyConsumer();
        final EventType type = EventType.CREATE;
        final String source = consumerServiceEventEmitter.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<Consumer> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(consumerRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        Mockito.doNothing().when(consumerValidator).validateForCreate(entity);
        Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());

        Mockito.when(consumerRepository.save(entity)).thenReturn(entity);
        Mockito.doThrow(new RuntimeException("BOOOOOOOM")).when(applicationEventMulticaster).multicastEvent(event);

        Optional<Consumer> response = consumerService.create(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(consumerServiceEventEmitter, Mockito.timeout(1000).times(1))
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

    public Consumer getDummyConsumer() {
        final Consumer entity = new Consumer();

        return entity;
    }

    public Specification getDummySpecification() {
        Specification spec = new Specification();
        spec.setConfigJson("{}");
        spec.setDescription("dummy spec");

        return spec;
    }
}