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
  static AvroKey key(String name, String domain, Integer version) {
    return AvroKey
        .newBuilder()
        .setId(Integer.toString(version))
        .setType(AvroKeyType.STREAM_VERSION)
        .setParent(AvroKey
            .newBuilder()
            .setId(name)
            .setType(AvroKeyType.STREAM)
            .setParent(AvroDomainConversion.key(domain))
            .build())
        .build();
  }

  @Override
  public AvroKey key(Stream stream) {
    return key(stream.getName(), stream.getDomain(), stream.getVersion());
  }

  @Override
  public AvroKey key(Stream.Key key) {
    return key(key.getName(), key.getDomain(), key.getVersion());
  }

  @Override
  public AvroStream toAvro(Stream stream) {
    return AvroStream
        .newBuilder()
        .setName(stream.getName())
        .setOwner(stream.getOwner())
        .setDescription(stream.getDescription())
        .setTags(AvroMaps.fromDto(stream.getTags()))
        .setConfiguration(AvroConfigurations.fromDto(stream.getConfiguration()))
        .setDomain(stream.getDomain())
        .setVersion(stream.getVersion())
        .setSchemaRef(AvroNameDomains.fromDto(stream.getSchema()))
        .build();
  }

  @Override
  public Stream toEntity(AvroStream stream) {
    return Stream
        .builder()
        .name(stream.getName().toString())
        .owner(stream.getOwner().toString())
        .description(stream.getDescription().toString())
        .tags(AvroMaps.toDto(stream.getTags()))
        .configuration(AvroConfigurations.toDto(stream.getConfiguration()))
        .domain(stream.getDomain().toString())
        .version(stream.getVersion())
        .schema(AvroNameDomains.toDto(stream.getSchemaRef()))
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
