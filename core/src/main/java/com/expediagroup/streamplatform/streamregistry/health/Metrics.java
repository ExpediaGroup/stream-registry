/* Copyright (C) 2018-2019 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.health;

public enum Metrics {
    STREAM_CREATION_HEALTH("app.is_stream_creation_healthy"),
    PRODUCER_REGISTRATION_HEALTH("app.is_producer_registration_healthy"),
    CONSUMER_REGISTRATION_HEALTH("app.is_consumer_registration_healthy"),
    STATE_STORE_HEALTH("app.is_globaltable_statestore_healthy"),
    STATE_STORE_STATE_HEALTH("app.is_globaltable_kstreams_in_valid_state"),
    STATE_STORE_STATE("app.globaltable_kstreams_state"),
    IS_EVENT_STORE_KAFKA_TOPIC_CONFIG_VALID("app.is_event_store_kafka_topic_config_valid");

    private String name;

    Metrics(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
