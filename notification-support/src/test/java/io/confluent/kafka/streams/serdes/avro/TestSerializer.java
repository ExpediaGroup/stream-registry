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

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.ImmutableMap;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;

@Slf4j
public class TestSerializer extends KafkaAvroSerializer {
  public static final MockSchemaRegistryClient schemaRegistryClient = new MockSchemaRegistryClient();
  public static final String schemaRegistryUrl = "http://foo:8081";

  public static final Map<String, Object> configProps = ImmutableMap.<String, Object>builder()
      .put(KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS, true)
      .put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)
      .build();

  public TestSerializer() {
    this(schemaRegistryClient);
  }

  public TestSerializer(SchemaRegistryClient client) {
    super(client);

    configure(configProps, false);
  }

  @Override
  public byte[] serialize(String topic, Object record) {
    log.info("Serializing {} for topic {}", record, topic);
    return super.serialize(topic, record);
  }
}