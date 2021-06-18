/**
 * Copyright (C) 2018-2021 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.repository.kafka;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.repository.kafka.Converter.DomainConverter;
import com.expediagroup.streamplatform.streamregistry.state.EntityView;
import com.expediagroup.streamplatform.streamregistry.state.EventSender;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.event.SpecificationEvent;
import com.expediagroup.streamplatform.streamregistry.state.model.event.StatusEvent;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.status.DefaultStatus;
import com.expediagroup.streamplatform.streamregistry.state.model.status.StatusEntry;

@RunWith(MockitoJUnitRunner.class)
public class DefaultRepositoryTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Mock private EntityView view;
  @Mock private EventSender sender;
  private DomainConverter converter = new DomainConverter();

  private DomainRepository underTest;

  @Before
  public void before() {
    underTest = new DomainRepository(view, sender, converter);
  }

  @Test
  public void saveExistingSpecificationAndStatus() {
    Entity<Entity.DomainKey, DefaultSpecification> domain = SampleState.domain();
    domain = domain.withSpecification(domain.getSpecification().withDescription("old description"));
    domain = domain.withStatus(new DefaultStatus().with(new StatusEntry(
      "agentStatus",
      mapper.createObjectNode().put("foo", "bar"),
      Instant.EPOCH,
      Instant.EPOCH,
      StatusEntry.State.UNDEFINED
    )));

    when(view.get(SampleState.domainKey())).thenReturn(Optional.of(domain));

    when(sender.send(any(SpecificationEvent.class))).thenReturn(completedFuture(null));
    when(sender.send(any(StatusEvent.class))).thenReturn(completedFuture(null));

    Domain result = underTest.save(SampleModel.domain());

    assertThat(result, is(SampleModel.domain()));

    Entity<Entity.DomainKey, DefaultSpecification> expected = SampleState.domain();

    verify(sender).send(Event.specification(expected.getKey(), expected.getSpecification()));
    verify(sender).send(Event.status(expected.getKey(), new StatusEntry(
      "agentStatus",
      mapper.createObjectNode(),
      Instant.EPOCH,
      Instant.EPOCH,
      StatusEntry.State.UNDEFINED
    )));
  }

  @Test
  public void saveExistingSpecificationOnly() {
    Entity<Entity.DomainKey, DefaultSpecification> domain = SampleState.domain();
    domain = domain.withSpecification(domain.getSpecification().withDescription("old description"));

    when(view.get(SampleState.domainKey())).thenReturn(Optional.of(domain));

    when(sender.send(any(SpecificationEvent.class))).thenReturn(completedFuture(null));

    Domain result = underTest.save(SampleModel.domain());

    assertThat(result, is(SampleModel.domain()));

    Entity<Entity.DomainKey, DefaultSpecification> expected = SampleState.domain();

    verify(sender).send(Event.specification(expected.getKey(), expected.getSpecification()));
    verify(sender, never()).send(Event.status(expected.getKey(), new StatusEntry(
      "agentStatus",
      mapper.createObjectNode(),
      Instant.EPOCH,
      Instant.EPOCH,
      StatusEntry.State.UNDEFINED
    )));
  }

  @Test
  public void saveExistingStatusOnly() {
    Entity<Entity.DomainKey, DefaultSpecification> domain = SampleState.domain();
    domain = domain.withStatus(new DefaultStatus().with(new StatusEntry(
      "agentStatus",
      mapper.createObjectNode().put("foo", "bar"),
      Instant.EPOCH,
      Instant.EPOCH,
      StatusEntry.State.UNDEFINED
    )));

    when(view.get(SampleState.domainKey())).thenReturn(Optional.of(domain));

    when(sender.send(any(StatusEvent.class))).thenReturn(completedFuture(null));

    Domain result = underTest.save(SampleModel.domain());

    assertThat(result, is(SampleModel.domain()));

    Entity<Entity.DomainKey, DefaultSpecification> expected = SampleState.domain();

    verify(sender, never()).send(Event.specification(expected.getKey(), expected.getSpecification()));
    verify(sender).send(Event.status(expected.getKey(), new StatusEntry(
      "agentStatus",
      mapper.createObjectNode(),
      Instant.EPOCH,
      Instant.EPOCH,
      StatusEntry.State.UNDEFINED
    )));
  }

  @Test
  public void saveNewSpecificationAndStatus() {
    when(view.get(SampleState.domainKey())).thenReturn(Optional.empty());

    when(sender.send(any(SpecificationEvent.class))).thenReturn(completedFuture(null));
    when(sender.send(any(StatusEvent.class))).thenReturn(completedFuture(null));

    Domain result = underTest.save(SampleModel.domain());

    assertThat(result, is(SampleModel.domain()));

    Entity<Entity.DomainKey, DefaultSpecification> expected = SampleState.domain();

    verify(sender).send(Event.specification(expected.getKey(), expected.getSpecification()));
    verify(sender).send(Event.status(expected.getKey(), new StatusEntry(
      "agentStatus",
      mapper.createObjectNode(),
      Instant.EPOCH,
      Instant.EPOCH,
      StatusEntry.State.UNDEFINED
    )));
  }

  @Test
  public void findById() {
    when(view.get(SampleState.domainKey())).thenReturn(Optional.of(SampleState.domain()));

    Optional<Domain> result = underTest.findById(SampleModel.domainKey());

    assertThat(result.isPresent(), is(true));
    assertThat(result.get(), is(SampleModel.domain()));
  }

  @Test
  public void findAll() {
    when(view.all(Entity.DomainKey.class)).thenReturn(Stream.of(SampleState.domain()));

    List<Domain> result = underTest.findAll();

    assertThat(result.size(), is(1));
    assertThat(result.get(0), is(SampleModel.domain()));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void findAllExample() {
    underTest.findAll(null);
  }
}
