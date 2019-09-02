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

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLDomain;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLSchema;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLTransformer;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.service.Service;

@RunWith(MockitoJUnitRunner.class)
public class SchemaResolverTest {

  @Mock
  Service<Domain, Domain.Key> domainKeyService;

  private static final ObjectMapper mapper = new ObjectMapper();

  @Test
  public void resolve() {

    when(domainKeyService.get(any())).thenReturn(Domain
        .builder()
        .name("streamDomain")
        .owner("owner")
        .description("description")
        .tags(Map.of("key", "value"))
        .type("type")
        .configuration(mapper.createObjectNode().put("key", "value"))
        .build()
    );

    GraphQLDomain resolvedDomain = new SchemaResolver(domainKeyService, new GraphQLTransformer())
        .domain(GraphQLSchema.builder().domainKey(new GraphQLDomain.Key("theDomain")).build()
        );

    assertEquals("streamDomain", resolvedDomain.getName());
  }
}