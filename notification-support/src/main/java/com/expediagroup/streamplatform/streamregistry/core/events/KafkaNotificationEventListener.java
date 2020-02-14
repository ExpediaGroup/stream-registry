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

import javax.annotation.PostConstruct;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;

import com.expediagroup.streamplatform.streamregistry.model.Schema;

@Slf4j
@Builder
public class KafkaNotificationEventListener {
    private static final String IS_CREATING_SCHEMA = "" +
            "event.entity instanceof T(com.expediagroup.streamplatform.streamregistry.model.Schema)" +
            "and event.eventType == T(com.expediagroup.streamplatform.streamregistry.core.events.EventType).CREATE";

    private static final String IS_UPDATING_SCHEMA = "" +
            "event.entity instanceof T(com.expediagroup.streamplatform.streamregistry.model.Schema)" +
            "and event.eventType == T(com.expediagroup.streamplatform.streamregistry.core.events.EventType).UPDATE";

    @Getter // Getter is only for testing purposes...
    private final Optional<KafkaSetupHandler> kafkaSetupHandler;

    @EventListener(condition = IS_CREATING_SCHEMA)
    public void onCreateSchema(NotificationEvent<Schema> event) {
        log.info("Update schema event: {}", event);
    }

    @EventListener(condition = IS_UPDATING_SCHEMA)
    public void onUpdateSchema(NotificationEvent<Schema> event) {
        log.info("Update schema event: {}", event);
    }

    @PostConstruct
    public void init() {
        log.warn("Kafka setup enabled {}", kafkaSetupHandler.isPresent());

        kafkaSetupHandler.ifPresent(KafkaSetupHandler::setup);
    }
}