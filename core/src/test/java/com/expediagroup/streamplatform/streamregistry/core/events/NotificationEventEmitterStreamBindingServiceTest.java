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
import com.expediagroup.streamplatform.streamregistry.core.repositories.StreamBindingRepository;
import com.expediagroup.streamplatform.streamregistry.core.services.StreamBindingService;
import com.expediagroup.streamplatform.streamregistry.core.validators.StreamBindingValidator;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfig.class)
public class NotificationEventEmitterStreamBindingServiceTest {

    @MockBean
    private ApplicationEventMulticaster applicationEventMulticaster;

    @MockBean
    private HandlerService handlerService;

    @MockBean
    private StreamBindingValidator streamBindingValidator;

    @MockBean
    private StreamBindingRepository streamBindingRepository;

    private StreamBindingService streamBindingService;

    @Before
    public void before() {
        streamBindingService = Mockito.spy(new StreamBindingService(applicationEventMulticaster, handlerService, streamBindingValidator, streamBindingRepository));
    }

    @Test
    public void givenAStreamBindingForCreate_validateThatNotificationEventIsEmitted() {
        final StreamBinding entity = getDummyStreamBinding();
        final EventType type = EventType.CREATE;
        final String source = streamBindingService.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<StreamBinding> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(streamBindingRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        Mockito.doNothing().when(streamBindingValidator).validateForCreate(entity);
        Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());
        Mockito.when(streamBindingRepository.save(entity)).thenReturn(entity);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

        streamBindingService.create(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(streamBindingService, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.eq(event));
    }

    @Test
    public void givenAStreamBindingForUpdate_validateThatNotificationEventIsEmitted() {
        final StreamBinding entity = getDummyStreamBinding();
        final EventType type = EventType.UPDATE;
        final String source = streamBindingService.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<StreamBinding> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(streamBindingRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
        Mockito.doNothing().when(streamBindingValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
        Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

        Mockito.when(streamBindingRepository.save(entity)).thenReturn(entity);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

        streamBindingService.update(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(streamBindingService, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.eq(event));
    }

    @Test
    public void givenANullStreamBindingRetrievedByRepositoryForCreate_validateThatNotificationEventIsNotEmitted() {
        final StreamBinding entity = getDummyStreamBinding();

        Mockito.when(streamBindingRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        Mockito.doNothing().when(streamBindingValidator).validateForCreate(entity);
        Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());

        Mockito.when(streamBindingRepository.save(entity)).thenReturn(null);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(Mockito.any());

        streamBindingService.create(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(0))
                .multicastEvent(Mockito.any());

        Mockito.verify(streamBindingService, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.any());
    }

    @Test
    public void givenANullStreamBindingRetrievedByRepositoryForUpdate_validateThatNotificationEventIsNotEmitted() {
        final StreamBinding entity = getDummyStreamBinding();

        Mockito.when(streamBindingRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
        Mockito.doNothing().when(streamBindingValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
        Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

        Mockito.when(streamBindingRepository.save(entity)).thenReturn(null);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(Mockito.any());

        streamBindingService.update(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(0))
                .multicastEvent(Mockito.any());

        Mockito.verify(streamBindingService, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.any());
    }

    @Test
    public void givenAStreamBindingForUpsert_validateThatNotificationEventIsEmitted() {
        final StreamBinding entity = getDummyStreamBinding();
        final EventType type = EventType.UPDATE;
        final String source = streamBindingService.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<StreamBinding> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(streamBindingRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
        Mockito.doNothing().when(streamBindingValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
        Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

        Mockito.when(streamBindingRepository.save(entity)).thenReturn(entity);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

        streamBindingService.upsert(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(streamBindingService, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.eq(event));
    }

    @Test
    public void givenAStreamBindingForCreate_handleAMulticasterException() {
        final StreamBinding entity = getDummyStreamBinding();
        final EventType type = EventType.CREATE;
        final String source = streamBindingService.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<StreamBinding> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(streamBindingRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        Mockito.doNothing().when(streamBindingValidator).validateForCreate(entity);
        Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());

        Mockito.when(streamBindingRepository.save(entity)).thenReturn(entity);
        Mockito.doThrow(new RuntimeException("BOOOOOOOM")).when(applicationEventMulticaster).multicastEvent(event);

        Optional<StreamBinding> response = streamBindingService.create(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(streamBindingService, Mockito.timeout(1000).times(1))
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

    public StreamBinding getDummyStreamBinding() {
        final StreamBinding entity = new StreamBinding();

        return entity;
    }

    public Specification getDummySpecification() {
        Specification spec = new Specification();
        spec.setConfigJson("{}");
        spec.setDescription("dummy spec");

        return spec;
    }
}