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

import static com.expediagroup.streamplatform.streamregistry.state.StateKey.deleted;
import static com.expediagroup.streamplatform.streamregistry.state.StateKey.existing;

import java.util.Map;
import java.util.Optional;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.event.SpecificationDeletionEvent;
import com.expediagroup.streamplatform.streamregistry.state.model.event.SpecificationEvent;
import com.expediagroup.streamplatform.streamregistry.state.model.event.StatusDeletionEvent;
import com.expediagroup.streamplatform.streamregistry.state.model.event.StatusEvent;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;
import com.expediagroup.streamplatform.streamregistry.state.model.status.DefaultStatus;

@Slf4j
@RequiredArgsConstructor
class EntityViewUpdater {
  @NonNull private final Map<StateKey, Entity<?, ?>> entities;

  <K extends Entity.Key<S>, S extends Specification> Entity<K, S> update(Event<K, S> event) {
    if (event instanceof SpecificationEvent) {
      return update((SpecificationEvent<K, S>) event);
    } else if (event instanceof StatusEvent) {
      return update((StatusEvent<K, S>) event);
    } else if (event instanceof SpecificationDeletionEvent) {
      return delete((SpecificationDeletionEvent<K, S>) event);
    } else if (event instanceof StatusDeletionEvent) {
      return delete((StatusDeletionEvent<K, S>) event);
    } else {
      throw new IllegalArgumentException("Unknown event " + event);
    }
  }

  <K extends Entity.Key<S>, S extends Specification> Optional<Entity<K, S>> purge(K key) {
    var oldEntity = (Entity<K, S>) entities.remove(deleted(key));
    log.debug("Purged entity for {}", key);
    return Optional.ofNullable(oldEntity);
  }

  private <K extends Entity.Key<S>, S extends Specification> Entity<K, S> update(SpecificationEvent<K, S> event) {
    var oldEntity = (Entity<K, S>) entities.get(existing(event.getKey()));
    var status = Optional
        .ofNullable(oldEntity)
        .map(Entity::getStatus)
        .orElseGet(DefaultStatus::new);
    var entity = new Entity<>(event.getKey(), event.getSpecification(), status);
    entities.put(existing(event.getKey()), entity);
    log.debug("Updated {} with {}", event.getKey(), event.getSpecification());
    return oldEntity;
  }

  private <K extends Entity.Key<S>, S extends Specification> Entity<K, S> update(StatusEvent<K, S> event) {
    var oldEntity = (Entity<K, S>) entities.get(existing(event.getKey()));
    if (oldEntity == null) {
      log.warn("Received status {} non existent entity {}", event.getStatusEntry().getName(), event.getKey());
      return null;
    }
    var entity = new Entity<>(event.getKey(), oldEntity.getSpecification(), oldEntity.getStatus().with(event.getStatusEntry()));
    entities.put(existing(event.getKey()), entity);
    log.debug("Updated {} with {}", event.getKey(), event.getStatusEntry());
    return oldEntity;
  }

  private <K extends Entity.Key<S>, S extends Specification> Entity<K, S> delete(SpecificationDeletionEvent<K, S> event) {
    var oldEntity = (Entity<K, S>) entities.remove(existing(event.getKey()));
    entities.put(deleted(event.getKey()), oldEntity);
    log.debug("Deleted entity for {}", event.getKey());
    return oldEntity;
  }

  private <K extends Entity.Key<S>, S extends Specification> Entity<K, S> delete(StatusDeletionEvent<K, S> event) {
    var oldEntity = (Entity<K, S>) entities.get(existing(event.getKey()));
    if (oldEntity == null) {
      log.warn("Received status deletion {} for non existent entity {}", event.getStatusName(), event.getKey());
      return null;
    }
    var entity = oldEntity.withStatus(oldEntity.getStatus().without(event.getStatusName()));
    entities.put(existing(event.getKey()), entity);
    log.debug("Deleted status {} for {}", event.getStatusName(), event.getKey());
    return oldEntity;
  }
}
