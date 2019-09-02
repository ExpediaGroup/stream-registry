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

import lombok.RequiredArgsConstructor;

import com.coxautodev.graphql.tools.GraphQLResolver;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLDomain;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLSchema;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLTransformer;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.service.Service;

@Component
@RequiredArgsConstructor
public class SchemaResolver implements GraphQLResolver<GraphQLSchema> {

  private final Service<Domain, Domain.Key> domainKeyService;
  private final GraphQLTransformer graphQLTransformer;

  public GraphQLDomain domain(GraphQLSchema graphQLSchema) {
    Domain.Key key = graphQLTransformer.transform(graphQLSchema.getDomainKey(), Domain.Key.class);
    Domain domain = domainKeyService.get(key);
    return graphQLTransformer.transform(domain, GraphQLDomain.class);
  }
}
