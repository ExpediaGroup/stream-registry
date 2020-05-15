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

import static com.expediagroup.streamplatform.streamregistry.state.model.event.Event.LOAD_COMPLETE;
import static lombok.AccessLevel.PACKAGE;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;

@RequiredArgsConstructor(access = PACKAGE)
public class EntityView implements Closeable {
  @NonNull private final EventReceiver receiver;
  @NonNull private final Map<Entity.Key<?>, Entity<?, ?>> entities;
  @NonNull private final EntityViewUpdater updater;

  EntityView(EventReceiver receiver, Map<Entity.Key<?>, Entity<?, ?>> entities) {
    this(receiver, entities, new EntityViewUpdater(entities));
  }

  public EntityView(EventReceiver receiver) {
    this(receiver, new HashMap<>());
  }

  public CompletableFuture<Void> load(@NonNull Listener listener) {
    var future = new CompletableFuture<Void>();
    receiver.receive(new ReceiverListener(listener, future));
    return future;
  }

  public CompletableFuture<Void> load() {
    return load(Listener.NULL);
  }

  @RequiredArgsConstructor
  class ReceiverListener implements EventReceiver.Listener {
    private final Listener listener;
    private final CompletableFuture<Void> future;

    @Override
    public <K extends Entity.Key<S>, S extends Specification> void onEvent(Event<K, S> event) {
      if (!future.isDone() && event == LOAD_COMPLETE) {
        future.complete(null);
      } else {
        var oldEntity = updater.update(event);
        if (future.isDone()) {
          listener.onEvent(oldEntity, event);
        }
      }
    }
  }

  public <K extends Entity.Key<S>, S extends Specification> Optional<Entity<K, S>> get(K key) {
    return Optional.ofNullable((Entity<K, S>) entities.get(key));
  }

  public <K extends Entity.Key<S>, S extends Specification> Stream<Entity<K, S>> all(Class<K> keyClass) {
    return entities.values().stream()
        .filter(x -> x.getKey().getClass().equals(keyClass))
        .map(x -> (Entity<K, S>) x);
  }

  @Override
  public void close() throws IOException {
    receiver.close();
  }

  public interface Listener {
    <K extends Entity.Key<S>, S extends Specification> void onEvent(Entity<K, S> oldEntity, Event<K, S> event);

    Listener NULL = new Listener() {
      @Override
      public <K extends Entity.Key<S>, S extends Specification> void onEvent(Entity<K, S> oldEntity, Event<K, S> event) { }
    };
  }
}
