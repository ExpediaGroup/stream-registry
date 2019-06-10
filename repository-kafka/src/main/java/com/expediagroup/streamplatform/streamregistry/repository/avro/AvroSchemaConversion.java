/**
 * Copyright (C) 2018-2019 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.repository.avro;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.model.Schema;

@Component
public class AvroSchemaConversion implements Conversion<Schema, Schema.Key, AvroSchema> {
  static AvroKey key(String name, String domain) {
    return AvroKey
        .newBuilder()
        .setId(name)
        .setType(AvroKeyType.SCHEMA)
        .setParent(AvroDomainConversion.key(domain))
        .build();
  }

  @Override
  public AvroKey key(Schema schema) {
    return key(schema.getName(), schema.getDomain());
  }

  @Override
  public AvroKey key(Schema.Key key) {
    return key(key.getName(), key.getDomain());
  }

  @Override
  public AvroSchema toAvro(Schema schema) {
    return AvroSchema
        .newBuilder()
        .setName(schema.getName())
        .setOwner(schema.getOwner())
        .setDescription(schema.getDescription())
        .setTags(AvroMaps.fromDto(schema.getTags()))
        .setConfiguration(AvroConfigurations.fromDto(schema.getConfiguration()))
        .setDomain(schema.getDomain())
        .build();
  }

  @Override
  public Schema toEntity(AvroSchema schema) {
    return Schema
        .builder()
        .name(schema.getName().toString())
        .owner(schema.getOwner().toString())
        .description(schema.getDescription().toString())
        .tags(AvroMaps.toDto(schema.getTags()))
        .configuration(AvroConfigurations.toDto(schema.getConfiguration()))
        .domain(schema.getDomain().toString())
        .build();
  }

  @Override
  public Class<AvroSchema> avroClass() {
    return AvroSchema.class;
  }

  @Override
  public AvroKeyType keyType() {
    return AvroKeyType.SCHEMA;
  }
}
