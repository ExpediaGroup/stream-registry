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
import com.expediagroup.streamplatform.streamregistry.core.repositories.ConsumerBindingRepository;
import com.expediagroup.streamplatform.streamregistry.core.services.ConsumerBindingService;
import com.expediagroup.streamplatform.streamregistry.core.validators.ConsumerBindingValidator;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Specification;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfig.class)
public class NotificationEventEmitterConsumerBindingServiceTest {

    @MockBean
    private ApplicationEventMulticaster applicationEventMulticaster;

    @MockBean
    private HandlerService handlerService;

    @MockBean
    private ConsumerBindingValidator consumerBindingValidator;

    @MockBean
    private ConsumerBindingRepository consumerBindingRepository;

    private ConsumerBindingService consumerBindingService;

    @Before
    public void before() {
        consumerBindingService = Mockito.spy(new ConsumerBindingService(applicationEventMulticaster, handlerService, consumerBindingValidator, consumerBindingRepository));
    }

    @Test
    public void givenAConsumerBindingForCreate_validateThatNotificationEventIsEmitted() {
        final ConsumerBinding entity = getDummyConsumerBinding();
        final EventType type = EventType.CREATE;
        final String source = consumerBindingService.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<ConsumerBinding> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(consumerBindingRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        Mockito.doNothing().when(consumerBindingValidator).validateForCreate(entity);
        Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());

        Mockito.when(consumerBindingRepository.save(entity)).thenReturn(entity);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

        consumerBindingService.create(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(consumerBindingService, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.eq(event));
    }

    @Test
    public void givenAConsumerBindingForUpdate_validateThatNotificationEventIsEmitted() {
        final ConsumerBinding entity = getDummyConsumerBinding();
        final EventType type = EventType.UPDATE;
        final String source = consumerBindingService.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<ConsumerBinding> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(consumerBindingRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
        Mockito.doNothing().when(consumerBindingValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
        Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

        Mockito.when(consumerBindingRepository.save(entity)).thenReturn(entity);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

        consumerBindingService.update(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(consumerBindingService, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.eq(event));
    }

    @Test
    public void givenANullConsumerBindingRetrievedByRepositoryForCreate_validateThatNotificationEventIsNotEmitted() {
        final ConsumerBinding entity = getDummyConsumerBinding();

        Mockito.when(consumerBindingRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        Mockito.doNothing().when(consumerBindingValidator).validateForCreate(entity);
        Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());

        Mockito.when(consumerBindingRepository.save(entity)).thenReturn(null);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(Mockito.any());

        consumerBindingService.create(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(0))
                .multicastEvent(Mockito.any());

        Mockito.verify(consumerBindingService, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.any());
    }

    @Test
    public void givenANullConsumerBindingRetrievedByRepositoryForUpdate_validateThatNotificationEventIsNotEmitted() {
        final ConsumerBinding entity = getDummyConsumerBinding();

        Mockito.when(consumerBindingRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
        Mockito.doNothing().when(consumerBindingValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
        Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

        Mockito.when(consumerBindingRepository.save(entity)).thenReturn(null);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(Mockito.any());

        consumerBindingService.update(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(0))
                .multicastEvent(Mockito.any());

        Mockito.verify(consumerBindingService, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.any());
    }

    @Test
    public void givenAConsumerBindingForUpsert_validateThatNotificationEventIsEmitted() {
        final ConsumerBinding entity = getDummyConsumerBinding();
        final EventType type = EventType.UPDATE;
        final String source = consumerBindingService.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<ConsumerBinding> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(consumerBindingRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
        Mockito.doNothing().when(consumerBindingValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
        Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

        Mockito.when(consumerBindingRepository.save(entity)).thenReturn(entity);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

        consumerBindingService.upsert(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(consumerBindingService, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.eq(event));
    }

    @Test
    public void givenAConsumerBindingForCreate_handleAMulticasterException() {
        final ConsumerBinding entity = getDummyConsumerBinding();
        final EventType type = EventType.CREATE;
        final String source = consumerBindingService.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<ConsumerBinding> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(consumerBindingRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        Mockito.doNothing().when(consumerBindingValidator).validateForCreate(entity);
        Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());

        Mockito.when(consumerBindingRepository.save(entity)).thenReturn(entity);
        Mockito.doThrow(new RuntimeException("BOOOOOOOM")).when(applicationEventMulticaster).multicastEvent(event);

        Optional<ConsumerBinding> response = consumerBindingService.create(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(consumerBindingService, Mockito.timeout(1000).times(1))
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

    public ConsumerBinding getDummyConsumerBinding() {
        final ConsumerBinding entity = new ConsumerBinding();

        return entity;
    }

    public Specification getDummySpecification() {
        Specification spec = new Specification();
        spec.setConfigJson("{}");
        spec.setDescription("dummy spec");

        return spec;
    }
}