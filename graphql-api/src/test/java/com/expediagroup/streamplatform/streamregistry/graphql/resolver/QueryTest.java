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
package com.expediagroup.streamplatform.streamregistry.graphql.resolver;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLConfiguration;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLDomain;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLKeyValue;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLNameDomain;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLSchema;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLStream;
import com.expediagroup.streamplatform.streamregistry.model.Configuration;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.NameDomain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.service.Service;

@RunWith(MockitoJUnitRunner.class)
public class QueryTest {
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
      .name("schemaName")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .configuration(Configuration
          .builder()
          .type("type")
          .properties(Map.of("key", "value"))
          .build())
      .domain("schemaDomain")
      .build();
  private final Stream stream = Stream
      .builder()
      .name("name")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .configuration(Configuration
          .builder()
          .type("type")
          .properties(Map.of("key", "value"))
          .build())
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
      .name("schemaName")
      .owner("owner")
      .description("description")
      .tags(List.of(new GraphQLKeyValue("key", "value")))
      .configuration(GraphQLConfiguration
          .builder()
          .type("type")
          .properties(List.of(new GraphQLKeyValue("key", "value")))
          .build())
      .domain(schemaGraphQLDomain)
      .build();
  private final GraphQLStream graphQLStream = GraphQLStream
      .builder()
      .name("name")
      .owner("owner")
      .description("description")
      .tags(List.of(new GraphQLKeyValue("key", "value")))
      .configuration(GraphQLConfiguration
          .builder()
          .type("type")
          .properties(List.of(new GraphQLKeyValue("key", "value")))
          .build())
      .domain(streamGraphQLDomain)
      .version(1)
      .schema(graphQLSchema)
      .build();
  @Mock
  private Service<Domain, Domain.Key> domainService;
  @Mock
  private Service<Schema, Schema.Key> schemaService;
  @Mock
  private Service<Stream, Stream.Key> streamService;
  private Query underTest;

  @Before
  public void before() {
    underTest = new Query(domainService, schemaService, streamService);
  }

  @Test
  public void domains() {
    when(domainService.stream(streamDomain)).thenReturn(java.util.stream.Stream.of(streamDomain));

    List<GraphQLDomain> result = underTest.domains(
        streamDomain.getName(),
        streamDomain.getOwner(),
        streamDomain.getDescription(),
        List.of(new GraphQLKeyValue("key", "value"))
    );

    assertThat(result.size(), is(1));
    assertThat(result.get(0), is(streamGraphQLDomain));
  }

  @Test
  public void schemas() {
    when(schemaService.stream(schema)).thenReturn(java.util.stream.Stream.of(schema));
    when(domainService.get(schemaDomain.key())).thenReturn(schemaDomain);

    List<GraphQLSchema> result = underTest.schemas(
        schema.getName(),
        schema.getOwner(),
        schema.getDescription(),
        List.of(new GraphQLKeyValue("key", "value")),
        GraphQLConfiguration
            .builder()
            .type("type")
            .properties(List.of(new GraphQLKeyValue("key", "value")))
            .build(),
        schema.getDomain()
    );

    assertThat(result.size(), is(1));
    assertThat(result.get(0), is(graphQLSchema));
  }

  @Test
  public void streams() {
    when(streamService.stream(stream)).thenReturn(java.util.stream.Stream.of(stream));
    when(schemaService.get(schema.key())).thenReturn(schema);
    when(domainService.get(schemaDomain.key())).thenReturn(schemaDomain);
    when(domainService.get(streamDomain.key())).thenReturn(streamDomain);

    List<GraphQLStream> result = underTest.streams(
        stream.getName(),
        stream.getOwner(),
        stream.getDescription(),
        List.of(new GraphQLKeyValue("key", "value")),
        GraphQLConfiguration
            .builder()
            .type("type")
            .properties(List.of(new GraphQLKeyValue("key", "value")))
            .build(),
        stream.getDomain(),
        1,
        GraphQLNameDomain
            .builder()
            .name("schemaName")
            .domain("schemaDomain")
            .build()
    );

    assertThat(result.size(), is(1));
    assertThat(result.get(0), is(graphQLStream));
  }
}
