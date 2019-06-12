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
import com.expediagroup.streamplatform.streamregistry.model.Stream;

@Value
@Builder
public class GraphQLStream {
  String name;
  String owner;
  String description;
  Iterable<GraphQLKeyValue> tags;
  GraphQLConfiguration configuration;
  GraphQLDomain domain;
  Integer version;
  GraphQLSchema schema;

  public static GraphQLStream fromDto(
      Stream stream,
      Domain streamDomain,
      Schema schema,
      Domain schemaDomain) {
    return GraphQLStream
        .builder()
        .name(stream.getName())
        .owner(stream.getOwner())
        .description(stream.getDescription())
        .tags(GraphQLKeyValue.fromDto(stream.getTags()))
        .configuration(GraphQLConfiguration.fromDto(stream.getConfiguration()))
        .domain(GraphQLDomain.fromDto(streamDomain))
        .version(stream.getVersion())
        .schema(GraphQLSchema.fromDto(schema, schemaDomain))
        .build();
  }
}
