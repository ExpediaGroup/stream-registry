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
package com.expediagroup.streamplatform.streamregistry.state.it;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.val;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.KafkaContainer;

import com.expediagroup.streamplatform.streamregistry.state.DefaultEntityView;
import com.expediagroup.streamplatform.streamregistry.state.DefaultEventCorrelator;
import com.expediagroup.streamplatform.streamregistry.state.EntityView;
import com.expediagroup.streamplatform.streamregistry.state.EntityViewListener;
import com.expediagroup.streamplatform.streamregistry.state.kafka.KafkaEventReceiver;
import com.expediagroup.streamplatform.streamregistry.state.kafka.KafkaEventSender;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Principal;
import com.expediagroup.streamplatform.streamregistry.state.model.status.DefaultStatus;
import com.expediagroup.streamplatform.streamregistry.state.model.status.StatusEntry;

public class StateIT {
  @Rule
  public KafkaContainer kafka = new KafkaContainer();

  private final ObjectMapper mapper = new ObjectMapper();
  private final ObjectNode configuration = mapper.createObjectNode();
  private final DomainKey key = new DomainKey("domain");
  private final Map<String, List<Principal>> security = Stream.of(
    new AbstractMap.SimpleEntry<>("admin", Arrays.asList(new Principal("user1"))),
    new AbstractMap.SimpleEntry<>("creator", Arrays.asList(new Principal("user2"), new Principal("user3")))
  ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  private final DefaultSpecification specification = new DefaultSpecification("description", Collections.emptyList(), "type", configuration, security);
  private final ObjectNode statusValue = mapper.createObjectNode();
  private final StatusEntry statusEntry = new StatusEntry("statusName", statusValue);
  private final Event<DomainKey, DefaultSpecification> specificationEvent = Event.specification(key, specification);
  private final Event<DomainKey, DefaultSpecification> statusEvent = Event.status(key, statusEntry);
  private final Event<DomainKey, DefaultSpecification> statusDeletionEvent = Event.statusDeletion(key, "statusName");
  private final Event<DomainKey, DefaultSpecification> specificationDeletionEvent = Event.specificationDeletion(key);

  @Test
  public void test() throws Exception {
    val topic = "topic";
    val schemaRegistryUrl = "mock://schemas";

    val correlator = new DefaultEventCorrelator();

    val receiver = new KafkaEventReceiver(KafkaEventReceiver.Config.builder()
        .bootstrapServers(kafka.getBootstrapServers())
        .schemaRegistryUrl(schemaRegistryUrl)
        .topic(topic)
        .groupId("groupId")
        .build(), correlator);

    val kafkaSender = new KafkaEventSender(KafkaEventSender.Config.builder()
        .bootstrapServers(kafka.getBootstrapServers())
        .schemaRegistryUrl(schemaRegistryUrl)
        .topic(topic)
        .build(), correlator);

    EntityView view = new DefaultEntityView(receiver);
    val listener = mock(EntityViewListener.class);
    view.load(listener).join();

    Optional<Entity<DomainKey, DefaultSpecification>> entity;

    kafkaSender.send(specificationEvent).join();
    verify(listener).onEvent(null, specificationEvent);

    entity = view.get(key);
    assertThat(entity.isPresent(), is(true));
    assertThat(entity.get().getSpecification(), is(specification));

    kafkaSender.send(statusEvent).join();
    verify(listener).onEvent(new Entity<>(key, specification, new DefaultStatus()), statusEvent);

    entity = view.get(key);
    assertThat(entity.isPresent(), is(true));
    assertThat(entity.get().getStatus().getValue("statusName"), is(statusValue));

    kafkaSender.send(statusDeletionEvent).join();
    verify(listener).onEvent(new Entity<>(key, specification, new DefaultStatus().with(statusEntry)), statusDeletionEvent);

    entity = view.get(key);
    assertThat(entity.isPresent(), is(true));
    assertThat(entity.get().getStatus().getNames().contains("statusName"), is(false));

    kafkaSender.send(specificationDeletionEvent).join();
    verify(listener).onEvent(new Entity<>(key, specification, new DefaultStatus()), specificationDeletionEvent);

    entity = view.get(key);
    assertThat(entity.isPresent(), is(false));
  }
}
