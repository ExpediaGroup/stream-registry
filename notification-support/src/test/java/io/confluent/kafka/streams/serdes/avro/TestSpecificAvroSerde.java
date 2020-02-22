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
package io.confluent.kafka.streams.serdes.avro;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;

import org.apache.avro.specific.SpecificRecord;

public class TestSpecificAvroSerde<T extends SpecificRecord> extends SpecificAvroSerde<T> {
    @Builder
    public TestSpecificAvroSerde(SchemaRegistryClient schemaRegistryClient, String schemaRegistryUrl, boolean isKey) {
        super(schemaRegistryClient);

        Map<String, Object> map = new HashMap<>();
        map.put(KafkaAvroDeserializerConfig.AUTO_REGISTER_SCHEMAS, true);
        map.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

        configure(map, isKey);
    }
}