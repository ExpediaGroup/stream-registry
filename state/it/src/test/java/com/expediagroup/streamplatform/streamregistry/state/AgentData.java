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
package com.expediagroup.streamplatform.streamregistry.state;

import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.Key;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.event.SpecificationEvent;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Principal;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Tag;
import com.expediagroup.streamplatform.streamregistry.state.model.status.DefaultStatus;

@AllArgsConstructor(access = PRIVATE)
@Getter
public final class AgentData {
  private final DomainKey key;
  private final DefaultSpecification defaultSpecification;
  private final Entity<DomainKey, DefaultSpecification> entity;
  private final SpecificationEvent<DomainKey, DefaultSpecification> specificationEvent;

  public AgentData withTags(List<Tag> tags) {

    val specification = new DefaultSpecification(randomUUID().toString(), tags, "type", new ObjectMapper().createObjectNode(), security());
    return new AgentData(key, specification, entity(key, specification), specificationEvent(key, specification));
  }

  public static AgentData generateData() {
    val key = key();
    val specification = defaultSpecification();
    return new AgentData(key, specification, entity(key, specification), specificationEvent(key, specification));
  }

  private static DomainKey key() {
    return new DomainKey(randomUUID().toString());
  }

  private static DefaultSpecification defaultSpecification() {
    return new DefaultSpecification(randomUUID().toString(), new ArrayList<>(), "type", new ObjectMapper().createObjectNode(), security());
  }

  private static Map<String, List<Principal>> security() {
    return Stream.of(
      new AbstractMap.SimpleEntry<>("admin", Arrays.asList(new Principal("user1"))),
      new AbstractMap.SimpleEntry<>("creator", Arrays.asList(new Principal("user2"), new Principal("user3")))
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static <K extends Key<DefaultSpecification>> Entity<K, DefaultSpecification> entity(
    K key,
    DefaultSpecification specification
  ) {
    return new Entity<>(key, specification, new DefaultStatus());
  }

  private static <K extends Key<DefaultSpecification>> SpecificationEvent<K, DefaultSpecification> specificationEvent(
    K key,
    DefaultSpecification specification
  ) {
    return Event.specification(key, specification);
  }
}
