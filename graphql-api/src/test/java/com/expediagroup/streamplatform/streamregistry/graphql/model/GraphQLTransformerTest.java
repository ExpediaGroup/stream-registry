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

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

public class GraphQLTransformerTest {
  private static final ObjectMapper mapper = new ObjectMapper();

  private final Domain domain1 = Domain.builder()
      .name("name1")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(mapper.createObjectNode().put("key", "value"))
      .build();

  private final Domain domain2 = Domain.builder()
      .name("name2")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(mapper.createObjectNode().put("key", "value"))
      .build();

  private final Schema schema = Schema.builder()
      .name("name")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(mapper.createObjectNode().put("key", "value"))
      .domainKey(domain1.key())
      .build();

  private final Stream stream = Stream.builder()
      .name("name")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(mapper.createObjectNode().put("key", "value"))
      .domainKey(domain2.key())
      .version(1)
      .schemaKey(schema.key())
      .build();

  private final GraphQLDomain graphQLDomain1 = GraphQLDomain.builder()
      .name("name1")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(mapper.createObjectNode().put("key", "value"))
      .build();

  private final GraphQLDomain graphQLDomain2 = GraphQLDomain.builder()
      .name("name2")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(mapper.createObjectNode().put("key", "value"))
      .build();

  private final GraphQLSchema graphQLSchema = GraphQLSchema.builder()
      .name("name")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(mapper.createObjectNode().put("key", "value"))
      .domainKey(graphQLDomain1.getKey())
      .build();

  private final GraphQLStream graphQLStream = GraphQLStream.builder()
      .name("name")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(mapper.createObjectNode().put("key", "value"))
      .domainKey(graphQLDomain2.getKey())
      .version(1)
      .schemaKey(graphQLSchema.getKey())
      .build();

  private GraphQLTransformer underTest;

  @Before
  public void before() {
    underTest = new GraphQLTransformer();
  }

  @Test
  public void domain() {
    assertThat(underTest.transform(domain1), is(graphQLDomain1));
  }

  @Test
  public void schema() {
    assertThat(underTest.transform(schema), is(graphQLSchema));
  }

  @Test
  public void stream() {
    assertThat(underTest.transform(stream), is(graphQLStream));
  }
}
