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
import com.expediagroup.streamplatform.streamregistry.core.repositories.ZoneRepository;
import com.expediagroup.streamplatform.streamregistry.core.services.ZoneService;
import com.expediagroup.streamplatform.streamregistry.core.validators.ZoneValidator;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Zone;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfig.class)
public class NotificationEventEmitterZoneServiceTest {

    @MockBean
    private ApplicationEventMulticaster applicationEventMulticaster;

    @MockBean
    private HandlerService handlerService;

    @MockBean
    private ZoneValidator zoneValidator;

    @MockBean
    private ZoneRepository zoneRepository;

    private ZoneService zoneService;

    @Before
    public void before() {
        zoneService = Mockito.spy(new ZoneService(applicationEventMulticaster, handlerService, zoneValidator, zoneRepository));
    }

    @Test
    public void givenAZoneForCreate_validateThatNotificationEventIsEmitted() {
        final Zone entity = getDummyZone();
        final EventType type = EventType.CREATE;
        final String source = zoneService.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<Zone> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(zoneRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        Mockito.doNothing().when(zoneValidator).validateForCreate(entity);
        Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());
        Mockito.when(zoneRepository.save(entity)).thenReturn(entity);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

        zoneService.create(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(zoneService, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.eq(event));
    }

    @Test
    public void givenAZoneForUpdate_validateThatNotificationEventIsEmitted() {
        final Zone entity = getDummyZone();
        final EventType type = EventType.UPDATE;
        final String source = zoneService.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<Zone> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(zoneRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
        Mockito.doNothing().when(zoneValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
        Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

        Mockito.when(zoneRepository.save(entity)).thenReturn(entity);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

        zoneService.update(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(zoneService, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.eq(event));
    }

    @Test
    public void givenANullZoneRetrievedByRepositoryForCreate_validateThatNotificationEventIsNotEmitted() {
        final Zone entity = getDummyZone();

        Mockito.when(zoneRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        Mockito.doNothing().when(zoneValidator).validateForCreate(entity);
        Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());

        Mockito.when(zoneRepository.save(entity)).thenReturn(null);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(Mockito.any());

        zoneService.create(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(0))
                .multicastEvent(Mockito.any());

        Mockito.verify(zoneService, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.any());
    }

    @Test
    public void givenANullZoneRetrievedByRepositoryForUpdate_validateThatNotificationEventIsNotEmitted() {
        final Zone entity = getDummyZone();

        Mockito.when(zoneRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
        Mockito.doNothing().when(zoneValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
        Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

        Mockito.when(zoneRepository.save(entity)).thenReturn(null);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(Mockito.any());

        zoneService.update(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(0))
                .multicastEvent(Mockito.any());

        Mockito.verify(zoneService, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.any());
    }

    @Test
    public void givenAZoneForUpsert_validateThatNotificationEventIsEmitted() {
        final Zone entity = getDummyZone();
        final EventType type = EventType.UPDATE;
        final String source = zoneService.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<Zone> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(zoneRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
        Mockito.doNothing().when(zoneValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
        Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

        Mockito.when(zoneRepository.save(entity)).thenReturn(entity);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

        zoneService.upsert(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(zoneService, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.eq(event));
    }

    @Test
    public void givenAZoneForCreate_handleAMulticasterException() {
        final Zone entity = getDummyZone();
        final EventType type = EventType.CREATE;
        final String source = zoneService.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<Zone> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(zoneRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        Mockito.doNothing().when(zoneValidator).validateForCreate(entity);
        Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());

        Mockito.when(zoneRepository.save(entity)).thenReturn(entity);
        Mockito.doThrow(new RuntimeException("BOOOOOOOM")).when(applicationEventMulticaster).multicastEvent(event);

        Optional<Zone> response = zoneService.create(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(zoneService, Mockito.timeout(1000).times(1))
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

    public Zone getDummyZone() {
        final Zone entity = new Zone();

        return entity;
    }

    public Specification getDummySpecification() {
        Specification spec = new Specification();
        spec.setConfigJson("{}");
        spec.setDescription("dummy spec");

        return spec;
    }
}