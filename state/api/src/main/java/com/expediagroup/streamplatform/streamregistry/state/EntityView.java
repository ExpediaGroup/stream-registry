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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.Key;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;

/**
 * Provides a unified view of the current state of all entities held in Stream Registry.
 */
public interface EntityView {
  /**
   * Commences loading the view. The returned {@link CompletableFuture} completes when the view has fully loaded.
   * Once fully loaded the provided {@link EntityViewListener} will be invoked for each subsequent event.
   *
   * @param listener to be invoked for each event after the view is fully loaded.
   * @return a future that completes when the view has fully loaded.
   */
  CompletableFuture<Void> load(EntityViewListener listener);

  /**
   * Returns an {@link Optional} containing the {@link Entity} that is associated with the specified key.
   * If there is no entity for the specified key, then an empty optional is returned.
   *
   * @param key
   * @param <K> the key type.
   * @param <S> the specification type.
   * @return an optional containing the entity that is associated with the specified key or empty if no entity exists.
   */
  <K extends Key<S>, S extends Specification> Optional<Entity<K, S>> get(K key);

  /**
   * Returns a {@link Stream} containing all entities of the given {@link Key} type.
   *
   * @param keyClass the key class of an entity type.
   * @param <K>      the key type.
   * @param <S>      the specification type.
   * @return a stream containing all entities of the given key type.
   */
  <K extends Key<S>, S extends Specification> Stream<Entity<K, S>> all(Class<K> keyClass);

  /**
   * Returns a {@link Map} containing all keys of the given {@link Key} type which have been deleted but not
   * purged ({@link #purgeDeleted(Key)}) mapped to the deleted Entity (if known)
   *
   * @param keyClass the key class of an entity type.
   * @param <K>      the key type.
   * @param <S>      the specification type.
   * @return a map containing all deleted keys of a give type to the key if known
   */
  <K extends Key<S>, S extends Specification> Map<K, Optional<Entity<K, S>>> allDeleted(Class<K> keyClass);

  /**
   * Purge a deleted entity so that it no longer appears in {@link #allDeleted(Class)}.
   *
   * @param key the key of the entity to be purged
   * @param <K> the key type.
   * @param <S> the specification type.
   * @return an optional containing the entity that was purged or empty if no entity exists.
   */
  <K extends Key<S>, S extends Specification> Optional<Entity<K, S>> purgeDeleted(K key);
}
