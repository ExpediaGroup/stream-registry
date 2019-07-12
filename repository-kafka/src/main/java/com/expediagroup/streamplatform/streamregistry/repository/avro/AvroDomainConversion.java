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
  public static AvroKey avroKey(Domain.Key key) {
    return AvroKey
        .newBuilder()
        .setId(key.getName())
        .setType(AvroKeyType.DOMAIN)
        .setParent(null)
        .build();
  }

  public static Domain.Key modelKey(AvroKey key) {
    return Domain.Key
        .builder()
        .name(key.getId())
        .build();
  }

  @Override
  public AvroKey key(Domain domain) {
    return avroKey(domain.key());
  }

  @Override
  public AvroKey key(Domain.Key key) {
    return avroKey(key);
  }

  @Override
  public AvroDomain toAvro(Domain domain) {
    return AvroDomain
        .newBuilder()
        .setName(domain.getName())
        .setOwner(domain.getOwner())
        .setDescription(domain.getDescription())
        .setTags(domain.getTags())
        .setType(domain.getType())
        .setConfiguration(domain.getConfiguration())
        .build();
  }

  @Override
  public Domain toEntity(AvroDomain domain) {
    return Domain
        .builder()
        .name(domain.getName())
        .owner(domain.getOwner())
        .description(domain.getDescription())
        .tags(domain.getTags())
        .type(domain.getType())
        .configuration(domain.getConfiguration())
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
