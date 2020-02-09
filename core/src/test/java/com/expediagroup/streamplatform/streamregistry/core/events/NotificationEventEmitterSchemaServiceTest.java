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
import com.expediagroup.streamplatform.streamregistry.core.repositories.SchemaRepository;
import com.expediagroup.streamplatform.streamregistry.core.services.SchemaService;
import com.expediagroup.streamplatform.streamregistry.core.validators.SchemaValidator;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Specification;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfig.class)
public class NotificationEventEmitterSchemaServiceTest {

    @MockBean
    private ApplicationEventMulticaster applicationEventMulticaster;

    @MockBean
    private HandlerService handlerService;

    @MockBean
    private SchemaValidator schemaValidator;

    @MockBean
    private SchemaRepository schemaRepository;

    private SchemaService schemaService;

    @Before
    public void before() {
        schemaService = Mockito.spy(new SchemaService(applicationEventMulticaster, handlerService, schemaValidator, schemaRepository));
    }

    @Test
    public void givenASchemaForCreate_validateThatNotificationEventIsEmitted() {
        final Schema entity = getDummySchema();
        final EventType type = EventType.CREATE;
        final String source = schemaService.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<Schema> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(schemaRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        Mockito.doNothing().when(schemaValidator).validateForCreate(entity);
        Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());
        Mockito.when(schemaRepository.save(entity)).thenReturn(entity);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

        schemaService.create(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(schemaService, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.eq(event));
    }

    @Test
    public void givenASchemaForUpdate_validateThatNotificationEventIsEmitted() {
        final Schema entity = getDummySchema();
        final EventType type = EventType.UPDATE;
        final String source = schemaService.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<Schema> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(schemaRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
        Mockito.doNothing().when(schemaValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
        Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

        Mockito.when(schemaRepository.save(entity)).thenReturn(entity);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

        schemaService.update(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(schemaService, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.eq(event));
    }

    @Test
    public void givenANullSchemaRetrievedByRepositoryForCreate_validateThatNotificationEventIsNotEmitted() {
        final Schema entity = getDummySchema();

        Mockito.when(schemaRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        Mockito.doNothing().when(schemaValidator).validateForCreate(entity);
        Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());

        Mockito.when(schemaRepository.save(entity)).thenReturn(null);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(Mockito.any());

        schemaService.create(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(0))
                .multicastEvent(Mockito.any());

        Mockito.verify(schemaService, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.any());
    }

    @Test
    public void givenANullSchemaRetrievedByRepositoryForUpdate_validateThatNotificationEventIsNotEmitted() {
        final Schema entity = getDummySchema();

        Mockito.when(schemaRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
        Mockito.doNothing().when(schemaValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
        Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

        Mockito.when(schemaRepository.save(entity)).thenReturn(null);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(Mockito.any());

        schemaService.update(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(0))
                .multicastEvent(Mockito.any());

        Mockito.verify(schemaService, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.any());
    }

    @Test
    public void givenASchemaForUpsert_validateThatNotificationEventIsEmitted() {
        final Schema entity = getDummySchema();
        final EventType type = EventType.UPDATE;
        final String source = schemaService.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<Schema> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(schemaRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
        Mockito.doNothing().when(schemaValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
        Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

        Mockito.when(schemaRepository.save(entity)).thenReturn(entity);
        Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

        schemaService.upsert(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(schemaService, Mockito.timeout(1000).times(0))
                .onFailedEmitting(Mockito.any(), Mockito.eq(event));
    }

    @Test
    public void givenASchemaForCreate_handleAMulticasterException() {
        final Schema entity = getDummySchema();
        final EventType type = EventType.CREATE;
        final String source = schemaService.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
        final NotificationEvent<Schema> event = getDummyNotificationEvent(source, type, entity);

        Mockito.when(schemaRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        Mockito.doNothing().when(schemaValidator).validateForCreate(entity);
        Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());

        Mockito.when(schemaRepository.save(entity)).thenReturn(entity);
        Mockito.doThrow(new RuntimeException("BOOOOOOOM")).when(applicationEventMulticaster).multicastEvent(event);

        Optional<Schema> response = schemaService.create(entity);

        Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
                .multicastEvent(event);

        Mockito.verify(schemaService, Mockito.timeout(1000).times(1))
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

    public Schema getDummySchema() {
        final Schema entity = new Schema();

        return entity;
    }

    public Specification getDummySpecification() {
        Specification spec = new Specification();
        spec.setConfigJson("{}");
        spec.setDescription("dummy spec");

        return spec;
    }
}