/**
 * Copyright (C) 2018-2019 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.core.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.core.handler.HandlerWrapper;
import com.expediagroup.streamplatform.streamregistry.core.predicate.PatternMatchPredicateFactory;
import com.expediagroup.streamplatform.streamregistry.core.validator.EntityValidator;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.repository.Repository;

@RunWith(MockitoJUnitRunner.class)
public class SchemaServiceTest {
  @Mock
  private EntityValidator entityValidator;
  @Mock
  private HandlerWrapper<Schema> schemaHandler;
  @Mock
  private Repository<Schema, Schema.Key> schemaRepository;
  @Mock
  private Repository<Domain, Domain.Key> domainRepository;
  @Mock
  private PatternMatchPredicateFactory patternMatchPredicateFactory;

  @Mock
  private Stream<Schema> stream1;
  @Mock
  private Stream<Schema> stream2;
  @Mock
  private Predicate<Schema> predicate;

  private SchemaService underTest;

  @Before
  public void before() {
    underTest = new SchemaService(
        entityValidator,
        schemaHandler,
        schemaRepository,
        domainRepository,
        patternMatchPredicateFactory);
  }

  @Test
  public void upsert() {
    Domain domain = Domain.builder().name("domain").build();
    Schema schema = Schema.builder().name("name").domain(domain.key()).build();
    Optional<Schema> existing = Optional.empty();

    when(schemaRepository.get(schema.key())).thenReturn(existing);
    when(schemaHandler.handle(schema, existing)).thenReturn(schema);
    when(domainRepository.get(domain.key())).thenReturn(Optional.of(domain));

    underTest.upsert(schema);

    InOrder inOrder = inOrder(
        entityValidator,
        schemaHandler,
        schemaRepository);
    inOrder.verify(schemaRepository).get(schema.key());
    inOrder.verify(entityValidator).validate(schema, existing);
    inOrder.verify(schemaHandler).handle(schema, existing);
    inOrder.verify(schemaRepository).upsert(schema);
  }

  @Test
  public void getExisting() {
    Schema schema = Schema.builder().name("name").domain(Domain.Key.builder().name("domain").build()).build();
    Optional<Schema> existing = Optional.of(schema);

    when(schemaRepository.get(schema.key())).thenReturn(existing);

    Schema result = underTest.get(schema.key());

    assertThat(result, is(schema));
  }

  @Test(expected = IllegalArgumentException.class)
  public void getNotExisting() {
    Schema schema = Schema.builder().name("name").domain(Domain.Key.builder().name("domain").build()).build();
    Optional<Schema> existing = Optional.empty();

    when(schemaRepository.get(schema.key())).thenReturn(existing);

    underTest.get(schema.key());
  }

  @Test
  public void stream() {
    Schema query = Schema.builder().build();

    when(schemaRepository.stream()).thenReturn(stream1);
    when(patternMatchPredicateFactory.create(eq(query), any())).thenReturn(predicate);
    when(stream1.filter(predicate)).thenReturn(stream2);

    Stream<Schema> result = underTest.stream(query);

    assertThat(result, is(stream2));
  }
}
