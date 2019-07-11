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
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.service.Service;

@RunWith(MockitoJUnitRunner.class)
public class GraphQLTransformerTest {
  private final Domain domain1 = Domain.builder()
      .name("name1")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(Map.of("key", "value"))
      .build();

  private final Domain domain2 = Domain.builder()
      .name("name2")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(Map.of("key", "value"))
      .build();

  private final Schema schema = Schema.builder()
      .name("name")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(Map.of("key", "value"))
      .domain(domain1.key())
      .build();

  private final Stream stream = Stream.builder()
      .name("name")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(Map.of("key", "value"))
      .domain(domain2.key())
      .version(1)
      .schema(schema.key())
      .build();

  private final GraphQLDomain graphQLDomain1 = GraphQLDomain.builder()
      .name("name1")
      .owner("owner")
      .description("description")
      .tags(List.of(new GraphQLKeyValue("key", "value")))
      .type("type")
      .configuration(List.of(new GraphQLKeyValue("key", "value")))
      .build();

  private final GraphQLDomain graphQLDomain2 = GraphQLDomain.builder()
      .name("name2")
      .owner("owner")
      .description("description")
      .tags(List.of(new GraphQLKeyValue("key", "value")))
      .type("type")
      .configuration(List.of(new GraphQLKeyValue("key", "value")))
      .build();

  private final GraphQLSchema graphQLSchema = GraphQLSchema.builder()
      .name("name")
      .owner("owner")
      .description("description")
      .tags(List.of(new GraphQLKeyValue("key", "value")))
      .type("type")
      .configuration(List.of(new GraphQLKeyValue("key", "value")))
      .domain(graphQLDomain1)
      .build();

  private final GraphQLStream graphQLStream = GraphQLStream.builder()
      .name("name")
      .owner("owner")
      .description("description")
      .tags(List.of(new GraphQLKeyValue("key", "value")))
      .type("type")
      .configuration(List.of(new GraphQLKeyValue("key", "value")))
      .domain(graphQLDomain2)
      .version(1)
      .schema(graphQLSchema)
      .build();

  @Mock
  private Service<Domain, Domain.Key> domainService;
  @Mock
  private Service<Schema, Schema.Key> schemaService;

  private GraphQLTransformer underTest;

  @Before
  public void before() {
    underTest = new GraphQLTransformer(domainService, schemaService);
  }

  @Test
  public void domain() {
    assertThat(underTest.transform(domain1), is(graphQLDomain1));
  }

  @Test
  public void schema() {
    when(domainService.get(domain1.key())).thenReturn(domain1);
    assertThat(underTest.transform(schema), is(graphQLSchema));
  }

  @Test
  public void stream() {
    when(domainService.get(domain1.key())).thenReturn(domain1);
    when(domainService.get(domain2.key())).thenReturn(domain2);
    when(schemaService.get(schema.key())).thenReturn(schema);
    assertThat(underTest.transform(stream), is(graphQLStream));
  }
}
