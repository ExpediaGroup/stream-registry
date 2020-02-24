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
import com.expediagroup.streamplatform.streamregistry.core.repositories.StreamRepository;
import com.expediagroup.streamplatform.streamregistry.core.services.StreamService;
import com.expediagroup.streamplatform.streamregistry.core.validators.StreamValidator;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfig.class)
public class NotificationEventEmitterStreamServiceTest {

    @MockBean
    private ApplicationEventMulticaster applicationEventMulticaster;

    @MockBean
    private HandlerService handlerService;

    @MockBean
    private StreamValidator streamValidator;

    @MockBean
    private StreamRepository streamRepository;

    private NotificationEventEmitter<Stream> streamServiceEventEmitter;
    private StreamService streamService;

    @Before
    public void before() {
        streamServiceEventEmitter = Mockito.spy(DefaultNotificationEventEmitter.<Stream>builder()
                .classType(Stream.class)
                .applicationEventMulticaster(applicationEventMulticaster)
                .build());

        streamService = Mockito.spy(new StreamService(handlerService, streamValidator, streamRepository, streamServiceEventEmitter));
    }

    @Test
    public void givenAStreamForCreate_validateThatNotificationEventIsEmitted() {
        final Stream entity = getDummyStream();
        final EventType type = EventType.CREATE;
        final String source = streamServiceEventEmitter.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<Stream> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(streamRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        Mockito.doNothing().when(streamValidator).validateForCreate(entity);
        Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());
        Mockito.when(streamRepository.save(entity)).thenReturn(entity);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

        streamService.create(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(streamServiceEventEmitter, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.eq(event));
    }

    @Test
    public void givenAStreamForUpdate_validateThatNotificationEventIsEmitted() {
        final Stream entity = getDummyStream();
        final EventType type = EventType.UPDATE;
        final String source = streamServiceEventEmitter.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<Stream> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(streamRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
        Mockito.doNothing().when(streamValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
        Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

        Mockito.when(streamRepository.save(entity)).thenReturn(entity);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

        streamService.update(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(streamServiceEventEmitter, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.eq(event));
    }

    @Test
    public void givenANullStreamRetrievedByRepositoryForCreate_validateThatNotificationEventIsNotEmitted() {
        final Stream entity = getDummyStream();

        Mockito.when(streamRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        Mockito.doNothing().when(streamValidator).validateForCreate(entity);
        Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());

        Mockito.when(streamRepository.save(entity)).thenReturn(null);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(Mockito.any());

        streamService.create(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(0))
                .multicastEvent(Mockito.any());

        Mockito.verify(streamServiceEventEmitter, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.any());
    }

    @Test
    public void givenANullStreamRetrievedByRepositoryForUpdate_validateThatNotificationEventIsNotEmitted() {
        final Stream entity = getDummyStream();

        Mockito.when(streamRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
        Mockito.doNothing().when(streamValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
        Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

        Mockito.when(streamRepository.save(entity)).thenReturn(null);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(Mockito.any());

        streamService.update(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(0))
                .multicastEvent(Mockito.any());

        Mockito.verify(streamServiceEventEmitter, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.any());
    }

    @Test
    public void givenAStreamForUpsert_validateThatNotificationEventIsEmitted() {
        final Stream entity = getDummyStream();
        final EventType type = EventType.UPDATE;
        final String source = streamServiceEventEmitter.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<Stream> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(streamRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
        Mockito.doNothing().when(streamValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
        Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

        Mockito.when(streamRepository.save(entity)).thenReturn(entity);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

        streamService.upsert(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(streamServiceEventEmitter, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.eq(event));
    }

    @Test
    public void givenAStreamForCreate_handleAMulticasterException() {
        final Stream entity = getDummyStream();
        final EventType type = EventType.CREATE;
        final String source = streamServiceEventEmitter.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<Stream> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(streamRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        Mockito.doNothing().when(streamValidator).validateForCreate(entity);
        Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());

        Mockito.when(streamRepository.save(entity)).thenReturn(entity);
        Mockito.doThrow(new RuntimeException("BOOOOOOOM")).when(applicationEventMulticaster).multicastEvent(event);

        Optional<Stream> response = streamService.create(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(streamServiceEventEmitter, Mockito.timeout(1000).times(1))
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

    public Stream getDummyStream() {
        final Stream entity = new Stream();

        return entity;
    }

    public Specification getDummySpecification() {
        Specification spec = new Specification();
        spec.setConfigJson("{}");
        spec.setDescription("dummy spec");

        return spec;
    }
}