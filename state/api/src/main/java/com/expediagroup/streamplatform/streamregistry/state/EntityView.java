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
 * <p/>
 * The underlying state storage from which this is this is loaded may support entities which have been deleted. If
 * that is the case it is important to understand the lifecycle of entities as it relates to {@link EntityView}.
 * <p/>
 * Entities may be created and updated in the underlying state storage. Any entity which has been created (but not
 * deleted) will be returned by {@link #all(Class)} and {@link #get(Key)}. It is expected that users handle said
 * entities accordingly (update external dependencies/respond to view requests etc).
 * <p/>
 * When entities are deleted in the underlying state storage, they will stop being returned by {@link #all(Class)} and
 * {@link #get(Key)}. Instead they will be returned by {@link #allDeleted(Class)}. In this case it is expected that
 * users handle those delete requests accordingly (e.g. remove the entity from external dependencies). This handling
 * does not automatically remove the entities from this in memory state (or the underlying state storage). In order to
 * remove the entity, {@link #purgeDeleted(Key)} should be used. This will remove all in memory references to said
 * entity and it will no longer be returned by {@link #allDeleted(Class)}.
 * <p/>
 * <i>Note:</i> {@link #purgeDeleted(Key)} does not delete the entity from the underlying storage, only from memory. It
 * is therefore possible that an entity that has previously had it's delete handled, will appear in
 * {@link #allDeleted(Class)} after restarts. Users of this class should be aware and handle this case (e.g. by
 * simply calling {@link #purgeDeleted(Key)} on said entities)
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
   * purged ({@link #purgeDeleted(Key)}) mapped to the deleted Entity (if known).
   * <p/>
   * Depending on underlying storage it is possible for an Entity to be marked deleted, by all references to the
   * original entity having removed. In that case we still know that an entity with a given key has been deleted
   * and the map will contain an {@link Map.Entry} with an empty value.
   *
   * @param keyClass the key class of an entity type.
   * @param <K>      the key type.
   * @param <S>      the specification type.
   * @return a map containing all deleted keys of a give type to the previously existing entity (if known)
   */
  <K extends Key<S>, S extends Specification> Map<K, Optional<Entity<K, S>>> allDeleted(Class<K> keyClass);

  /**
   * Purge a deleted entity so that it no longer appears in {@link #allDeleted(Class)}. It is removed from memory
   * BUT NOT the underlying off memory state storage. Calling purge on any entity that is not deleted will do nothing.
   *
   * @param key the key of the entity to be purged
   * @param <K> the key type.
   * @param <S> the specification type.
   * @return an optional containing the entity that was purged or empty if no entity exists.
   */
  <K extends Key<S>, S extends Specification> Optional<Entity<K, S>> purgeDeleted(K key);
}
