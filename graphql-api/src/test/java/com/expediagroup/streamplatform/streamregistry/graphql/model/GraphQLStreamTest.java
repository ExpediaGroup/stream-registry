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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.NameDomain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

public class GraphQLStreamTest {
  private final Domain streamDomain = Domain
      .builder()
      .name("streamDomain")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .build();

  private final Domain schemaDomain = Domain
      .builder()
      .name("schemaDomain")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .build();

  private final Schema schema = Schema
      .builder()
      .name("name")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(Map.of("key", "value"))
      .domain("schemaDomain")
      .build();

  private final Stream stream = Stream
      .builder()
      .name("name")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(Map.of("key", "value"))
      .domain("streamDomain")
      .version(1)
      .schema(NameDomain
          .builder()
          .name("schemaName")
          .domain("schemaDomain")
          .build())
      .build();

  private final GraphQLDomain schemaGraphQLDomain = GraphQLDomain
      .builder()
      .name("schemaDomain")
      .owner("owner")
      .description("description")
      .tags(List.of(new GraphQLKeyValue("key", "value")))
      .build();

  private final GraphQLDomain streamGraphQLDomain = GraphQLDomain
      .builder()
      .name("streamDomain")
      .owner("owner")
      .description("description")
      .tags(List.of(new GraphQLKeyValue("key", "value")))
      .build();

  private final GraphQLSchema graphQLSchema = GraphQLSchema
      .builder()
      .name("name")
      .owner("owner")
      .description("description")
      .tags(List.of(new GraphQLKeyValue("key", "value")))
      .type("type")
      .configuration(List.of(new GraphQLKeyValue("key", "value")))
      .domain(schemaGraphQLDomain)
      .build();

  private final GraphQLStream graphQLStream = GraphQLStream
      .builder()
      .name("name")
      .owner("owner")
      .description("description")
      .tags(List.of(new GraphQLKeyValue("key", "value")))
      .type("type")
      .configuration(List.of(new GraphQLKeyValue("key", "value")))
      .domain(streamGraphQLDomain)
      .version(1)
      .schema(graphQLSchema)
      .build();

  @Test
  public void fromDto() {
    assertThat(GraphQLStream.fromDto(stream, streamDomain, schema, schemaDomain), is(graphQLStream));
  }
}
