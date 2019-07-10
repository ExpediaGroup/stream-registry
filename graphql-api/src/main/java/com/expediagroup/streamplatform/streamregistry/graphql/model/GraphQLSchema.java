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
package com.expediagroup.streamplatform.streamregistry.graphql.model;

import lombok.Builder;
import lombok.Value;

import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;

@Value
@Builder
public class GraphQLSchema {
  String name;
  String owner;
  String description;
  Iterable<GraphQLKeyValue> tags;
  String type;
  Iterable<GraphQLKeyValue> configuration;
  GraphQLDomain domain;

  public static GraphQLSchema fromDto(
      Schema schema,
      Domain domain) {
    return GraphQLSchema
        .builder()
        .name(schema.getName())
        .owner(schema.getOwner())
        .description(schema.getDescription())
        .tags(GraphQLKeyValue.fromDto(schema.getTags()))
        .type(schema.getType())
        .configuration(GraphQLKeyValue.fromDto(schema.getConfiguration()))
        .domain(GraphQLDomain.fromDto(domain))
        .build();
  }

  @Value
  @Builder
  public static class Key {
    String name;
    String domain;
  }
}
