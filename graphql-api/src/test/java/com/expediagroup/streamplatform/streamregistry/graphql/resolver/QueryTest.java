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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLDomain;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLSchema;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLStream;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.service.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class QueryTest {
  private static final ObjectMapper mapper = new ObjectMapper();

  private final Domain streamDomain = Domain
      .builder()
      .name("streamDomain")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(mapper.createObjectNode().put("key", "value"))
      .build();
  private final Domain schemaDomain = Domain
      .builder()
      .name("schemaDomain")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(mapper.createObjectNode().put("key", "value"))
      .build();
  private final Schema schema = Schema
      .builder()
      .name("schemaName")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(mapper.createObjectNode().put("key", "value"))
      .domainKey(new Domain.Key("schemaDomain"))
      .build();
  private final Stream stream = Stream
      .builder()
      .name("name")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(mapper.createObjectNode().put("key", "value"))
      .domainKey(new Domain.Key("streamDomain"))
      .version(1)
      .schemaKey(new Schema.Key("schemaName", new Domain.Key("schemaDomain")))
      .build();
  private final GraphQLDomain schemaGraphQLDomain = GraphQLDomain
      .builder()
      .name("schemaDomain")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(mapper.createObjectNode().put("key", "value"))
      .build();
  private final GraphQLDomain streamGraphQLDomain = GraphQLDomain
      .builder()
      .name("streamDomain")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(mapper.createObjectNode().put("key", "value"))
      .build();
  private final GraphQLSchema graphQLSchema = GraphQLSchema
      .builder()
      .name("schemaName")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(mapper.createObjectNode().put("key", "value"))
      .domainKey(schemaGraphQLDomain.getKey())
      .build();
  private final GraphQLStream graphQLStream = GraphQLStream
      .builder()
      .name("name")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(mapper.createObjectNode().put("key", "value"))
      .domainKey(streamGraphQLDomain.getKey())
      .version(1)
      .schemaKey(graphQLSchema.getKey())
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
        streamDomain.getTags(),
        streamDomain.getType(),
        streamDomain.getConfiguration()
    );

    verify(domainService).stream(streamDomain);

    assertThat(result.size(), is(1));
    assertThat(result.get(0), is(streamGraphQLDomain));
  }

  @Test
  public void schemas() {
    when(schemaService.stream(schema)).thenReturn(java.util.stream.Stream.of(schema));

    List<GraphQLSchema> result = underTest.schemas(
        schema.getName(),
        schema.getOwner(),
        schema.getDescription(),
        schema.getTags(),
        schema.getType(),
        schema.getConfiguration(),
        schema.getDomainKey().getName()
    );

    assertThat(result.size(), is(1));
    assertThat(result.get(0), is(graphQLSchema));
  }

  @Test
  public void streams() {
    when(streamService.stream(stream)).thenReturn(java.util.stream.Stream.of(stream));

    List<GraphQLStream> result = underTest.streams(
        stream.getName(),
        stream.getOwner(),
        stream.getDescription(),
        stream.getTags(),
        stream.getType(),
        stream.getConfiguration(),
        stream.getDomainKey().getName(),
        stream.getVersion(),
        new GraphQLSchema.Key("schemaName", new GraphQLDomain.Key("schemaDomain"))
    );

    assertThat(result.size(), is(1));
    assertThat(result.get(0), is(graphQLStream));
  }
}
