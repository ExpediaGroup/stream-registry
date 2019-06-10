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

import com.expediagroup.streamplatform.streamregistry.model.Domain;

@Component
public class AvroDomainConversion implements Conversion<Domain, Domain.Key, AvroDomain> {
  static AvroKey key(String domain) {
    return AvroKey
        .newBuilder()
        .setId(domain)
        .setType(AvroKeyType.DOMAIN)
        .setParent(null)
        .build();
  }

  @Override
  public AvroKey key(Domain domain) {
    return key(domain.getName());
  }

  @Override
  public AvroKey key(Domain.Key key) {
    return key(key.getName());
  }

  @Override
  public AvroDomain toAvro(Domain domain) {
    return AvroDomain
        .newBuilder()
        .setName(domain.getName())
        .setOwner(domain.getOwner())
        .setDescription(domain.getDescription())
        .setTags(AvroMaps.fromDto(domain.getTags()))
        .build();
  }

  @Override
  public Domain toEntity(AvroDomain domain) {
    return Domain
        .builder()
        .name(domain.getName().toString())
        .owner(domain.getOwner().toString())
        .description(domain.getDescription().toString())
        .tags(AvroMaps.toDto(domain.getTags()))
        .build();
  }

  @Override
  public Class<AvroDomain> avroClass() {
    return AvroDomain.class;
  }

  @Override
  public AvroKeyType keyType() {
    return AvroKeyType.DOMAIN;
  }
}
