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

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLConfiguration;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLKeyValue;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLNameDomain;
import com.expediagroup.streamplatform.streamregistry.model.Configuration;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.NameDomain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.service.Service;

@RunWith(MockitoJUnitRunner.class)
public class MutationTest {
  private final Domain domain = Domain
      .builder()
      .name("name")
      .owner("root")
      .description("description")
      .tags(Map.of("key", "value"))
      .build();
  private final Schema schema = Schema
      .builder()
      .name("name")
      .owner("root")
      .description("description")
      .tags(Map.of("key", "value"))
      .configuration(Configuration
          .builder()
          .type("type")
          .properties(Map.of("key", "value"))
          .build())
      .domain("domain")
      .build();
  private final Stream stream = Stream
      .builder()
      .name("name")
      .owner("root")
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
  @Mock
  private Service<Domain, Domain.Key> domainService;
  @Mock
  private Service<Schema, Schema.Key> schemaService;
  @Mock
  private Service<Stream, Stream.Key> streamService;
  private Mutation underTest;

  @Before
  public void before() {
    underTest = new Mutation(domainService, schemaService, streamService);
  }

  @Test
  public void upsertDomain() {
    boolean result = underTest.upsertDomain(
        domain.getName(),
        domain.getDescription(),
        List.of(new GraphQLKeyValue("key", "value"))
    );

    assertThat(result, is(true));
    verify(domainService).upsert(domain);
  }

  @Test
  public void upsertSchema() {
    boolean result = underTest.upsertSchema(
        schema.getName(),
        schema.getDescription(),
        List.of(new GraphQLKeyValue("key", "value")),
        GraphQLConfiguration
            .builder()
            .type("type")
            .properties(List.of(new GraphQLKeyValue("key", "value")))
            .build(),
        schema.getDomain()
    );

    assertThat(result, is(true));
    verify(schemaService).upsert(schema);
  }

  @Test
  public void upsertStream() {
    boolean result = underTest.upsertStream(
        stream.getName(),
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

    assertThat(result, is(true));
    verify(streamService).upsert(stream);
  }
}
