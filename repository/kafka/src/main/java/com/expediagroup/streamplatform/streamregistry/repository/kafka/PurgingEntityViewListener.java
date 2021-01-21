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
package com.expediagroup.streamplatform.streamregistry.repository.kafka;

import lombok.AllArgsConstructor;
import lombok.NonNull;

import com.expediagroup.streamplatform.streamregistry.state.EntityView;
import com.expediagroup.streamplatform.streamregistry.state.EntityViewListener;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.event.SpecificationDeletionEvent;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;

@AllArgsConstructor
class PurgingEntityViewListener implements EntityViewListener {
  private final EntityView entityView;

  @Override
  public <K extends Entity.Key<S>, S extends Specification> void onEvent(Entity<K, S> oldEntity, @NonNull Event<K, S> event) {
    if (event instanceof SpecificationDeletionEvent) {
      entityView.purgeDeleted(event.getKey());
    }
  }
}
