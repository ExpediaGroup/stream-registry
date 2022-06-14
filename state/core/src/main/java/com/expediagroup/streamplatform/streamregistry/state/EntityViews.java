/**
 * Copyright (C) 2018-2022 Expedia, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.state;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static lombok.AccessLevel.PACKAGE;

public final class EntityViews {

  @NonNull
  public static EntityView defaultEntityView(EventReceiver receiver) {
    return new DefaultEntityView(receiver);
  }

  @NonNull
  public static EntityView meteredEntityView(EventReceiver receiver, MeterRegistry meterRegistry) {
    Map<Entity.Key<?>, StateValue> entities = new ConcurrentHashMap<>();
    meterRegistry.gaugeMapSize("stream_registry_state.view.entities", Tags.empty(), entities);

    DefaultEntityViewUpdater defaultEntityViewUpdater = new DefaultEntityViewUpdater(entities);
    return new DefaultEntityView(receiver, entities, new MeteredEntityViewUpdater(defaultEntityViewUpdater, meterRegistry));
  }

  @RequiredArgsConstructor(access = PACKAGE)
  static final class MeteredEntityViewUpdater implements EntityViewUpdater {
    private final EntityViewUpdater delegate;
    private final MeterRegistry meterRegistry;

    @Override
    public <K extends Entity.Key<S>, S extends Specification> Entity<K, S> update(Event<K, S> event) {
      meterRegistry.counter("stream_registry_state.receiver.update", event.getClass().getSimpleName().toLowerCase()).increment();
      return delegate.update(event);
    }

    @Override
    public <K extends Entity.Key<S>, S extends Specification> Optional<Entity<K, S>> purge(K key) {
      meterRegistry.counter("stream_registry_state.receiver.purge", key.getClass().getSimpleName().toLowerCase()).increment();
      return delegate.purge(key);
    }
  }
}