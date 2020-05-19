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

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;

/**
 * An interface for sending entity state change {@link Event Events}.
 */
public interface EventSender extends Closeable {
  /**
   * Method for sending an {@link Event}
   *
   * @param event the event.
   * @param <K>   the key type.
   * @param <S>   the specification type.
   * @return a future that completes when the event has successfully sent or when received if using a correlator.
   */
  <K extends Entity.Key<S>, S extends Specification> CompletableFuture<Void> send(Event<K, S> event);
}
