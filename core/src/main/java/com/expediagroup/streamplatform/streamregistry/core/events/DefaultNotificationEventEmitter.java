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

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.ApplicationEventMulticaster;

import com.expediagroup.streamplatform.streamregistry.model.*;

@Slf4j
public class DefaultNotificationEventEmitter<T> implements NotificationEventEmitter<T> {
    private static Set<Class<?>> SUPPORTED_ENTITY_CLASSES = Set.of(
            ConsumerBinding.class,
            Consumer.class,
            Domain.class,
            Infrastructure.class,
            ProducerBinding.class,
            Producer.class,
            Schema.class,
            StreamBinding.class,
            Stream.class,
            Zone.class
    );

    private final Class<T> classType;
    private final ApplicationEventMulticaster applicationEventMulticaster;

    @Builder
    private DefaultNotificationEventEmitter(Class<T> classType, ApplicationEventMulticaster applicationEventMulticaster) {
        Objects.requireNonNull(applicationEventMulticaster, "ApplicationEventMulticaster can not be null");

        this.classType = Optional.ofNullable(classType)
                .filter(SUPPORTED_ENTITY_CLASSES::contains)
                .orElseThrow(() -> typeException(classType));

        this.applicationEventMulticaster = applicationEventMulticaster;
    }

    private static RuntimeException typeException(Class<?> type) {
        if (type != null)
            return new IllegalArgumentException(String.format("Unsupported %s entity type for notification event emitter", type));
        else
            return new NullPointerException("Entity type definition for notification event emitter can not be null");
    }

    @Override
    public Optional<T> emitEventOnProcessedEntity(EventType eventType, T entity) {
        log.info("Emitting {} type event for {} entity {}", eventType, classType, entity);
        return emitEvent(applicationEventMulticaster::multicastEvent, eventType, entity);
    }

    @Override
    public void onFailedEmitting(Throwable ex, NotificationEvent<T> event) {
        log.info("There was an error emitting an event {}", event, ex);
    }
}
