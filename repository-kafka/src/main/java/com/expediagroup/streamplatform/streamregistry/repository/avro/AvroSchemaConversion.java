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
  public static AvroKey avroKey(Schema.Key key) {
    return AvroKey
        .newBuilder()
        .setId(key.getName())
        .setType(AvroKeyType.SCHEMA)
        .setParent(AvroDomainConversion.avroKey(key.getDomain()))
        .build();
  }

  public static Schema.Key modelKey(AvroKey key) {
    return Schema.Key
        .builder()
        .name(key.getId())
        .domain(AvroDomainConversion.modelKey(key.getParent()))
        .build();
  }

  @Override
  public AvroKey key(Schema.Key key) {
    return avroKey(key);
  }

  @Override
  public Class<AvroSchema> avroClass() {
    return AvroSchema.class;
  }

  @Override
  public Class<Schema> entityClass() {
    return Schema.class;
  }

  @Override
  public AvroKeyType keyType() {
    return AvroKeyType.SCHEMA;
  }
}
