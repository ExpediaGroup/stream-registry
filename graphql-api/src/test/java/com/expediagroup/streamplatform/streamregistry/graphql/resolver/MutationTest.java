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

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLDomain;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLSchema;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.service.Service;

@RunWith(MockitoJUnitRunner.class)
public class MutationTest {
  private static final ObjectMapper mapper = new ObjectMapper();

  private final Domain domain = Domain
      .builder()
      .name("name")
      .owner("root")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(mapper.createObjectNode().put("key", "value"))
      .build();
  private final Schema schema = Schema
      .builder()
      .name("name")
      .owner("root")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(mapper.createObjectNode().put("key", "value"))
      .domainKey(new Domain.Key("domainKey"))
      .build();
  private final Stream stream = Stream
      .builder()
      .name("name")
      .owner("root")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(mapper.createObjectNode().put("key", "value"))
      .domainKey(new Domain.Key("streamDomain"))
      .version(1)
      .schemaKey(new Schema.Key("schemaName", new Domain.Key("schemaDomain")))
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
    boolean result = underTest.createDomain(
        domain.getName(),
        domain.getDescription(),
        domain.getTags(),
        domain.getType(),
        domain.getConfiguration()
    );

    assertThat(result, is(true));
    verify(domainService).upsert(domain);
  }

  @Test
  public void upsertSchema() {
    boolean result = underTest.createSchema(
        schema.getName(),
        schema.getDescription(),
        schema.getTags(),
        schema.getType(),
        schema.getConfiguration(),
        schema.getDomainKey().getName()
    );

    assertThat(result, is(true));
    verify(schemaService).upsert(schema);
  }

  @Test
  public void upsertStream() {
    boolean result = underTest.createStream(
        stream.getName(),
        stream.getDescription(),
        stream.getTags(),
        stream.getType(),
        stream.getConfiguration(),
        stream.getDomainKey().getName(),
        1,
        new GraphQLSchema.Key("schemaName", new GraphQLDomain.Key("schemaDomain"))

    );

    assertThat(result, is(true));
    verify(streamService).upsert(stream);
  }
}
