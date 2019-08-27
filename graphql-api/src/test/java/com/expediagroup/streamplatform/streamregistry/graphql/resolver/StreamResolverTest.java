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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLDomain;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLSchema;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLStream;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLTransformer;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.service.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class StreamResolverTest {

  @Mock
  Service<Domain, Domain.Key> domainKeyService;

  @Mock
  Service<Schema, Schema.Key> schemaKeyService;

  private static final ObjectMapper mapper = new ObjectMapper();
  private StreamResolver streamResolver;
  private GraphQLStream stream;

  private GraphQLTransformer transformer = new GraphQLTransformer();

  private GraphQLDomain expectedGraphQlDomain;
  private GraphQLSchema expectedGraphQlSchema;

  @Before
  public void before() {

    streamResolver = new StreamResolver(
        schemaKeyService,
        domainKeyService,
        new GraphQLTransformer()
    );

    GraphQLDomain.Key key = new GraphQLDomain.Key("TheDomain");

    stream = GraphQLStream.builder()
        .domainKey(key)
        .schemaKey(new GraphQLSchema.Key("TheName", key))
        .build();

    Domain domain = Domain.builder()
        .name("streamDomain")
        .owner("owner")
        .description("description")
        .tags(Map.of("key", "value"))
        .type("type")
        .configuration(mapper.createObjectNode().put("key", "value"))
        .build();

    Schema schema = Schema.builder().build();

    expectedGraphQlDomain = transformer.transform(domain);
    expectedGraphQlSchema = transformer.transform(schema);
    when(domainKeyService.get(any())).thenReturn(domain);
    when(schemaKeyService.get(any())).thenReturn(schema);
  }

  @Test
  public void resolveDomain() {
    assertEquals(expectedGraphQlDomain, streamResolver.domain(stream));
  }

  @Test
  public void resolveSchema() {
    assertEquals(expectedGraphQlSchema, streamResolver.schema(stream));
  }
}