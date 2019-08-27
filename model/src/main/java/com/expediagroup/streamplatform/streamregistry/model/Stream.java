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
package com.expediagroup.streamplatform.streamregistry.model;

import static java.util.Collections.emptyMap;

import java.util.Map;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;

@Wither
@Value
@Builder
public class Stream implements Entity<Stream.Key> {

  String name;
  String owner;
  String description;
  @Builder.Default Map<String, String> tags = emptyMap();
  String type;
  @Builder.Default ObjectNode configuration = mapper.createObjectNode();

  Domain.Key domainKey;
  Integer version;
  Schema.Key schemaKey;

  @Override
  public Key key() {
    return new Key(name, domainKey, version);
  }

  @Value
  @Builder
  public static class Key {
    String name;
    Domain.Key domain;
    Integer version;
  }
}
