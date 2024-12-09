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

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;

import lombok.NonNull;

/**
 * A listener that is invoked for each event after the {@link EntityView} is fully loaded.
 */
public interface EntityViewListener {
  /**
   * Method invoked upon receiving an {@link Event} after the {@link EntityView} is fully loaded.
   * It is provided with the {@link Entity} state prior to receiving the event or {@code null} if it did not
   * exist along with the event itself.
   *
   * @param oldEntity the entity state prior to receiving the event.
   * @param event     the event itself.
   * @param <K>       the key type.
   * @param <S>       the specification type.
   */
  <K extends Entity.Key<S>, S extends Specification> void onEvent(Entity<K, S> oldEntity, @NonNull Event<K, S> event);

  /**
   * A null object listener.
   */
  EntityViewListener NULL = new EntityViewListener() {
    @Override
    public <K extends Entity.Key<S>, S extends Specification> void onEvent(Entity<K, S> oldEntity, Event<K, S> event) {}
  };
}
