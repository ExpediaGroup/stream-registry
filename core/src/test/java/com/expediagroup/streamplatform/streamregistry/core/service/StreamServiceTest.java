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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.core.handler.HandlerWrapper;
import com.expediagroup.streamplatform.streamregistry.core.predicate.PatternMatchPredicateFactory;
import com.expediagroup.streamplatform.streamregistry.core.predicate.VersionPredicateFactory;
import com.expediagroup.streamplatform.streamregistry.core.validator.EntityValidator;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.repository.Repository;

@RunWith(MockitoJUnitRunner.class)
public class StreamServiceTest {
  private final PatternMatchPredicateFactory patternMatchPredicateFactory = new PatternMatchPredicateFactory();
  private final VersionPredicateFactory versionPredicateFactory = new VersionPredicateFactory();
  @Mock
  private EntityValidator entityValidator;
  @Mock
  private HandlerWrapper<Stream> streamHandler;
  @Mock
  private Repository<Stream, Stream.Key> streamRepository;
  @Mock
  private Repository<Schema, Schema.Key> schemaRepository;
  @Mock
  private Repository<Domain, Domain.Key> domainRepository;
  private StreamService underTest;

  @Before
  public void before() {
    underTest = new StreamService(
        entityValidator,
        streamHandler,
        streamRepository,
        schemaRepository,
        domainRepository,
        patternMatchPredicateFactory,
        versionPredicateFactory);
  }

  @Test
  public void upsert() {
    Domain domain = Domain.builder().name("domain").build();
    Stream stream = Stream
        .builder()
        .name("name")
        .version(1)
        .schema(Schema.Key
            .builder()
            .name("schemaName")
            .domain(Domain.Key
                .builder()
                .name("schemaDomain")
                .build())
            .build())
        .domain(domain.key())
        .build();
    Optional<Stream> existing = Optional.empty();

    when(streamRepository.get(stream.key())).thenReturn(existing);
    when(schemaRepository.get(stream.getSchema())).thenReturn(Optional.of(Schema.builder().build()));
    when(streamHandler.handle(stream, existing)).thenReturn(stream);
    when(domainRepository.get(domain.key())).thenReturn(Optional.of(domain));

    underTest.upsert(stream);

    InOrder inOrder = inOrder(
        entityValidator,
        streamHandler,
        streamRepository,
        schemaRepository);
    inOrder.verify(streamRepository).get(stream.key());
    inOrder.verify(entityValidator).validate(stream, existing);
    inOrder.verify(schemaRepository).get(stream.getSchema());
    inOrder.verify(streamHandler).handle(stream, existing);
    inOrder.verify(streamRepository).upsert(stream);
  }

  @Test
  public void upsertHigherVersion() {
    Domain domain = Domain.builder().name("domain").build();
    Stream streamV1 = Stream
        .builder()
        .name("name")
        .domain(domain.key())
        .version(1)
        .build();
    Stream streamV2 = Stream
        .builder()
        .name("name")
        .domain(domain.key())
        .version(2)
        .schema(Schema.Key
            .builder()
            .name("schemaName")
            .domain(Domain.Key
                .builder()
                .name("schemaDomain")
                .build())
            .build())
        .build();
    Optional<Stream> existing = Optional.empty();

    when(streamRepository.get(streamV2.key())).thenReturn(existing);
    when(streamRepository.stream()).thenReturn(java.util.stream.Stream.of(streamV1));
    when(schemaRepository.get(streamV2.getSchema())).thenReturn(Optional.of(Schema.builder().build()));
    when(streamHandler.handle(streamV2, existing)).thenReturn(streamV2);
    when(domainRepository.get(domain.key())).thenReturn(Optional.of(domain));

    underTest.upsert(streamV2);

    verify(streamRepository).upsert(streamV2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void upsertHigherInvalidVersion() {
    Domain domain = Domain.builder().name("domain").build();
    Stream streamV1 = Stream
        .builder()
        .name("name")
        .domain(domain.key())
        .version(1)
        .build();
    Stream streamV2 = Stream
        .builder()
        .name("name")
        .domain(domain.key())
        .version(3)
        .schema(Schema.Key
            .builder()
            .name("schemaName")
            .domain(Domain.Key
                .builder()
                .name("schemaDomain")
                .build())
            .build())
        .build();
    Optional<Stream> existing = Optional.empty();

    when(streamRepository.get(streamV2.key())).thenReturn(existing);
    when(streamRepository.stream()).thenReturn(java.util.stream.Stream.of(streamV1));
    when(domainRepository.get(domain.key())).thenReturn(Optional.of(domain));

    underTest.upsert(streamV2);
  }


  @Test(expected = IllegalArgumentException.class)
  public void upsertNullVersion() {
    Domain domain = Domain.builder().name("domain").build();
    Stream stream = Stream
        .builder()
        .name("name")
        .version(null)
        .schema(Schema.Key
            .builder()
            .name("schemaName")
            .domain(Domain.Key
                .builder()
                .name("schemaDomain")
                .build())
            .build())
        .domain(domain.key())
        .build();
    Optional<Stream> existing = Optional.empty();

    when(streamRepository.get(stream.key())).thenReturn(existing);

    underTest.upsert(stream);
  }

  @Test(expected = IllegalArgumentException.class)
  public void upsertZeroVersion() {
    Domain domain = Domain.builder().name("domain").build();
    Stream stream = Stream
        .builder()
        .name("name")
        .version(0)
        .schema(Schema.Key
            .builder()
            .name("schemaName")
            .domain(Domain.Key
                .builder()
                .name("schemaDomain")
                .build())
            .build())
        .domain(domain.key())
        .build();
    Optional<Stream> existing = Optional.empty();

    when(streamRepository.get(stream.key())).thenReturn(existing);

    underTest.upsert(stream);
  }

  @Test(expected = NullPointerException.class)
  public void upsertNullSchema() {
    Stream stream = Stream
        .builder()
        .name("name")
        .version(1)
        .schema(null)
        .build();
    Optional<Stream> existing = Optional.empty();

    when(streamRepository.get(stream.key())).thenReturn(existing);

    underTest.upsert(stream);
  }

  @Test(expected = IllegalArgumentException.class)
  public void upsertSchemaNotExist() {
    Domain domain = Domain.builder().name("domain").build();
    Stream stream = Stream
        .builder()
        .name("name")
        .version(0)
        .schema(Schema.Key
            .builder()
            .name("schemaName")
            .domain(Domain.Key
                .builder()
                .name("schemaDomain")
                .build())
            .build())
        .domain(domain.key())
        .build();
    Optional<Stream> existing = Optional.empty();

    when(streamRepository.get(stream.key())).thenReturn(existing);

    underTest.upsert(stream);
  }

  @Test
  public void getExisting() {
    Stream stream = Stream.builder().name("name").version(1).build();
    Optional<Stream> existing = Optional.of(stream);

    when(streamRepository.get(stream.key())).thenReturn(existing);

    Stream result = underTest.get(stream.key());

    assertThat(result, is(stream));
  }

  @Test(expected = IllegalArgumentException.class)
  public void getNotExisting() {
    Stream stream = Stream.builder().name("name").version(1).build();
    Optional<Stream> existing = Optional.empty();

    when(streamRepository.get(stream.key())).thenReturn(existing);

    underTest.get(stream.key());
  }

  @Test
  public void stream() {
    Stream query = Stream.builder().name("^foo$").build();
    Stream stream = Stream.builder().name("foo").build();

    when(streamRepository.stream()).thenReturn(java.util.stream.Stream.of(stream));

    Stream result = underTest.stream(query).findFirst().get();

    assertThat(result, is(stream));
  }
}
