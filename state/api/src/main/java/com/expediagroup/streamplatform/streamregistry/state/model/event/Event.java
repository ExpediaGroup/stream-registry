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
package com.expediagroup.streamplatform.streamregistry.state.model.event;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;
import com.expediagroup.streamplatform.streamregistry.state.model.status.StatusEntry;

import lombok.NonNull;

public interface Event<K extends Entity.Key<S>, S extends Specification> {
  K getKey();

  static <K extends Entity.Key<S>, S extends Specification> SpecificationEvent<K, S> specification(@NonNull K key, @NonNull S specification) {
    return new SpecificationEvent<>(key, specification);
  }

  static <K extends Entity.Key<S>, S extends Specification> StatusEvent<K, S> status(@NonNull K key, @NonNull StatusEntry statusEntry) {
    return new StatusEvent<>(key, statusEntry);
  }

  static <K extends Entity.Key<S>, S extends Specification> SpecificationDeletionEvent<K, S> specificationDeletion(@NonNull K key) {
    return new SpecificationDeletionEvent<>(key);
  }

  static <K extends Entity.Key<S>, S extends Specification> StatusDeletionEvent<K, S> statusDeletion(@NonNull K key, @NonNull String statusName) {
    return new StatusDeletionEvent<>(key, statusName);
  }

  Event<?, ?> LOAD_COMPLETE = (Event<DomainKey, DefaultSpecification>) () -> null;
}
