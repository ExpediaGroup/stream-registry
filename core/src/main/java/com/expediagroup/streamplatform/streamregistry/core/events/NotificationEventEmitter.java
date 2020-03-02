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
package com.expediagroup.streamplatform.streamregistry.core.events;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import lombok.NonNull;

public interface NotificationEventEmitter<T> {
  Optional<T> emitEventOnProcessedEntity(EventType type, T entity);

  void onFailedEmitting(Throwable ex, NotificationEvent<T> event);

  default Optional<T> emitEvent(@NonNull Consumer<NotificationEvent<T>> emitter, @NonNull EventType type, T entity) {
    return emitEvent(emitter, null, type, entity);
  }

  default Optional<T> emitEvent(@NonNull Consumer<NotificationEvent<T>> emitter, String sourceEventPrefix, @NonNull EventType type, T entity) {
    if (entity != null) {
      String prefix = sourceEventPrefix != null ? sourceEventPrefix : getSourceEventPrefix(entity);
      String source = prefix.concat(type.toString().toLowerCase());
      final NotificationEvent<T> event = NotificationEvent.<T>builder()
          .source(source)
          .eventType(type)
          .entity(entity)
          .build();

      Function<Throwable, Void> onException = ex -> {
        onFailedEmitting(ex, event);
        return null;
      };

      CompletableFuture.runAsync(() -> emitter.accept(event))
          .exceptionally(onException);
    }

    return Optional.ofNullable(entity);
  }

  default String getSourceEventPrefix(T entity) {
    return entity.getClass().getSimpleName().toLowerCase() + "-";
  }
}