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

import com.expediagroup.streamplatform.streamregistry.model.Stream;

@Component
public class AvroStreamConversion implements Conversion<Stream, Stream.Key, AvroStream> {
  public static AvroKey avroKey(Stream.Key key) {
    return AvroKey
        .newBuilder()
        .setId(Integer.toString(key.getVersion()))
        .setType(AvroKeyType.STREAM_VERSION)
        .setParent(AvroKey
            .newBuilder()
            .setId(key.getName())
            .setType(AvroKeyType.STREAM)
            .setParent(AvroDomainConversion.avroKey(key.getDomain()))
            .build())
        .build();
  }

  public static Stream.Key modelKey(AvroKey key) {
    return Stream.Key
        .builder()
        .name(key.getParent().getId())
        .domain(AvroDomainConversion.modelKey(key.getParent().getParent()))
        .version(Integer.parseInt(key.getId()))
        .build();
  }

  @Override
  public AvroKey key(Stream stream) {
    return avroKey(stream.key());
  }

  @Override
  public AvroKey key(Stream.Key key) {
    return avroKey(key);
  }

  @Override
  public AvroStream toAvro(Stream stream) {
    return AvroStream
        .newBuilder()
        .setName(stream.getName())
        .setOwner(stream.getOwner())
        .setDescription(stream.getDescription())
        .setTags(stream.getTags())
        .setType(stream.getType())
        .setConfiguration(stream.getConfiguration())
        .setDomainKey(AvroDomainConversion.avroKey(stream.getDomain()))
        .setVersion(stream.getVersion())
        .setSchemaKey(AvroSchemaConversion.avroKey(stream.getSchema()))
        .build();
  }

  @Override
  public Stream toEntity(AvroStream stream) {
    return Stream
        .builder()
        .name(stream.getName())
        .owner(stream.getOwner())
        .description(stream.getDescription())
        .tags(stream.getTags())
        .type(stream.getType())
        .configuration(stream.getConfiguration())
        .domain(AvroDomainConversion.modelKey(stream.getDomainKey()))
        .version(stream.getVersion())
        .schema(AvroSchemaConversion.modelKey(stream.getSchemaKey()))
        .build();
  }

  @Override
  public Class<AvroStream> avroClass() {
    return AvroStream.class;
  }

  @Override
  public AvroKeyType keyType() {
    return AvroKeyType.STREAM_VERSION;
  }
}
