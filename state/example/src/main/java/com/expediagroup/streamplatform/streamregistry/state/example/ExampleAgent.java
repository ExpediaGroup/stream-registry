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
package com.expediagroup.streamplatform.streamregistry.state.example;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.state.EntityView;
import com.expediagroup.streamplatform.streamregistry.state.EntityViewListener;
import com.expediagroup.streamplatform.streamregistry.state.EventSender;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Principal;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Tag;

@EnableScheduling
@Component
@RequiredArgsConstructor
public class ExampleAgent implements EntityViewListener {
  private static final ObjectMapper mapper = new ObjectMapper();

  private final EventSender eventSender;
  private final EntityView entityView;

  @PostConstruct
  void init() {
    //Commence bootstrapping the Stream Registry state
    CompletableFuture<Void> future = entityView.load(this);

    //Block until the Stream Registry state is fully loaded
    future.join();
  }

  //Perform a full reconciliation on a schedule
  @Scheduled(initialDelayString = "${initialDelay:PT60S}", fixedDelayString = "${fixedDelay:PT60S}")
  void reconcile() {
    //Query specific entities
    Optional<Entity<DomainKey, DefaultSpecification>> optionalDomain = entityView.get(new DomainKey("my_domain"));

    //Query all entities of a specific type
    Stream<Entity<DomainKey, DefaultSpecification>> allDomains = entityView.all(DomainKey.class);

    //Mutate an entity
    CompletableFuture<Void> future = eventSender.send(Event.specification(
        new DomainKey("my_domain"),
        new DefaultSpecification(
            "description",
            Collections.singletonList(new Tag("name", "value")),
            "type",
            mapper.createObjectNode(),
            Stream.of(
              new AbstractMap.SimpleEntry<>("admin", Arrays.asList(new Principal("user1"))),
              new AbstractMap.SimpleEntry<>("creator", Arrays.asList(new Principal("user2"), new Principal("user3")))
            ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        )
    ));

    //Block until sent
    future.join();
  }

  @Override
  public <K extends Entity.Key<S>, S extends Specification> void onEvent(Entity<K, S> oldEntity, Event<K, S> event) {
    //React to mutations as they occur
  }
}
