/**
 * Copyright (C) 2018-2024 Expedia, Inc.
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

import static com.expediagroup.streamplatform.streamregistry.state.StateValue.deleted;
import static com.expediagroup.streamplatform.streamregistry.state.StateValue.existing;

import java.util.Map;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

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
@AllArgsConstructor
class DefaultEntityViewUpdater implements EntityViewUpdater {
  @NonNull
  private final Map<Entity.Key<?>, StateValue> entities;
  private Boolean entityStatusEnabled = true;

  @Override
  public <K extends Entity.Key<S>, S extends Specification> Entity<K, S> update(Event<K, S> event) {
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

  @Override
  public <K extends Entity.Key<S>, S extends Specification> Optional<Entity<K, S>> purge(K key) {
    val stateEntity = Optional.ofNullable(entities.get(key))
      .filter(it -> it.deleted);
    stateEntity.ifPresent(it -> {
      entities.remove(key);
      log.debug("Purged entity for key={}", key);
    });
    return stateEntity.map(it -> (Entity<K, S>) it.entity);
  }

  private <K extends Entity.Key<S>, S extends Specification> Entity<K, S> update(SpecificationEvent<K, S> event) {
    val oldEntity = (Entity<K, S>) getExistingEntity(event.getKey());
    val status = Optional.ofNullable(oldEntity)
      .map(Entity::getStatus)
      .orElseGet(DefaultStatus::new);
    val entity = new Entity<>(event.getKey(), event.getSpecification(), status);
    entities.put(event.getKey(), existing(entity));
    log.debug("Updated {} with {}", event.getKey(), event.getSpecification());
    return oldEntity;
  }

  private <K extends Entity.Key<S>, S extends Specification> Entity<K, S> update(StatusEvent<K, S> event) {
    val oldEntity = (Entity<K, S>) getExistingEntity(event.getKey());

    if (!entityStatusEnabled) {
      log.warn("Entity Status is disabled and is not persisted for key={}", event.getKey());
      return oldEntity;
    }

    if (oldEntity == null) {
      log.info("Received status {} non existent entity {}", event.getStatusEntry().getName(), event.getKey());
      return null;
    }
    val entity = new Entity<>(event.getKey(), oldEntity.getSpecification(), oldEntity.getStatus().with(event.getStatusEntry()));
    entities.put(event.getKey(), existing(entity));
    log.debug("Updated {} with {}", event.getKey(), event.getStatusEntry());
    return oldEntity;
  }

  private <K extends Entity.Key<S>, S extends Specification> Entity<K, S> delete(SpecificationDeletionEvent<K, S> event) {
    val oldEntity = (Entity<K, S>) getEntity(event.getKey());
    entities.put(event.getKey(), deleted(oldEntity));
    log.debug("Deleted entity for {}", event.getKey());
    return oldEntity;
  }

  private <K extends Entity.Key<S>, S extends Specification> Entity<K, S> delete(StatusDeletionEvent<K, S> event) {
    val oldEntity = (Entity<K, S>) getExistingEntity(event.getKey());
    if (oldEntity == null) {
      log.info("Received status deletion {} for non existent entity {}", event.getStatusName(), event.getKey());
      return null;
    }
    val entity = oldEntity.withStatus(oldEntity.getStatus().without(event.getStatusName()));
    entities.put(event.getKey(), existing(entity));
    log.debug("Deleted status {} for {}", event.getStatusName(), event.getKey());
    return oldEntity;
  }

  private Entity<?, ?> getExistingEntity(Entity.Key<?> key) {
    return Optional.ofNullable(entities.get(key))
      .filter(it -> !it.deleted)
      .map(it -> it.entity)
      .orElse(null);
  }

  /**
   * There is a chance the entity will have already been deleted. Only use this method if you don't care.
   */
  private Entity<?, ?> getEntity(Entity.Key<?> key) {
    val stateValue = Optional.ofNullable(entities.get(key));
    stateValue.ifPresent(it -> {
        if (it.deleted) {
          log.debug("Found deleted entity for key={}", key);
        } else {
          log.debug("Found entity for key={}", key);
        }
      }
    );
    return stateValue
      .map(it -> it.entity)
      .orElse(null);
  }
}
