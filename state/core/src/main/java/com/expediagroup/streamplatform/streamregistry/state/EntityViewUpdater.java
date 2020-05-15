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
package com.expediagroup.streamplatform.streamregistry.state;

import java.util.Map;
import java.util.Optional;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.event.SpecificationEvent;
import com.expediagroup.streamplatform.streamregistry.state.model.event.StatusEvent;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;
import com.expediagroup.streamplatform.streamregistry.state.model.status.DefaultStatus;

@Slf4j
@RequiredArgsConstructor
class EntityViewUpdater {
  @NonNull private final Map<Entity.Key<?>, Entity<?, ?>> entities;

  <K extends Entity.Key<S>, S extends Specification> Entity<K, S> update(Event<K, S> event) {
    if (event instanceof SpecificationEvent) {
      return update((SpecificationEvent<K, S>) event);
    } else {
      return update((StatusEvent<K, S>) event);
    }
  }

  private <K extends Entity.Key<S>, S extends Specification> Entity<K, S> update(SpecificationEvent<K, S> event) {
    var oldEntity = (Entity<K, S>) entities.get(event.getKey());
    var status = Optional
        .ofNullable(oldEntity)
        .map(Entity::getStatus)
        .orElseGet(DefaultStatus::new);
    var entity = new Entity<>(event.getKey(), event.getSpecification(), status);
    entities.put(event.getKey(), entity);
    log.debug("Updated {} with {}", event.getKey(), event.getSpecification());
    return oldEntity;
  }

  private <K extends Entity.Key<S>, S extends Specification> Entity<K, S> update(StatusEvent<K, S> event) {
    var oldEntity = (Entity<K, S>) entities.get(event.getKey());
    if (oldEntity == null) {
      log.warn("Received status for non existent entity {} - {}", event.getKey(), event.getStatusEntry().getName());
      return null;
    }
    var entity = new Entity<>(event.getKey(), oldEntity.getSpecification(), oldEntity.getStatus().with(event.getStatusEntry()));
    entities.put(event.getKey(), entity);
    log.debug("Updated {} with {}", event.getKey(), event.getStatusEntry());
    return oldEntity;
  }
}
