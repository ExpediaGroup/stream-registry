/**
 * Copyright (C) 2018-2020 Expedia, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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

@Slf4j
@Builder
public class KafkaNotificationEventListener {

    @Getter // Getter is only for testing purposes...
    private final Optional<KafkaSetupHandler> kafkaSetupHandler;

    @PostConstruct
    public void init() {
        log.warn("Kafka setup enabled {}", kafkaSetupHandler.isPresent());

        kafkaSetupHandler.ifPresent(KafkaSetupHandler::setup);
    }
}