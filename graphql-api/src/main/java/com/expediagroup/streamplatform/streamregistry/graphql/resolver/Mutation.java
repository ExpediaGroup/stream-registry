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

import com.coxautodev.graphql.tools.GraphQLMutationResolver;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLConfiguration;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLKeyValue;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLNameDomain;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.service.Service;

@Component
@RequiredArgsConstructor
public class Mutation implements GraphQLMutationResolver {
  private final Service<Domain, Domain.Key> domainService;
  private final Service<Schema, Schema.Key> schemaService;
  private final Service<Stream, Stream.Key> streamService;

  public boolean upsertDomain(
      String name,
      String description,
      Iterable<GraphQLKeyValue> tags) {
    domainService.upsert(
        Domain
            .builder()
            .name(name)
            .owner("root") //TODO inject user
            .description(description)
            .tags(GraphQLKeyValue.toDto(tags))
            .build()
    );
    return true;
  }

  public boolean upsertSchema(
      String name,
      String description,
      Iterable<GraphQLKeyValue> tags,
      GraphQLConfiguration configuration,
      String domain) {
    schemaService.upsert(
        Schema
            .builder()
            .name(name)
            .owner("root") //TODO inject user
            .description(description)
            .tags(GraphQLKeyValue.toDto(tags))
            .configuration(GraphQLConfiguration.toDto(configuration))
            .domain(domain)
            .build()
    );
    return true;
  }

  public boolean upsertStream(
      String name,
      String description,
      Iterable<GraphQLKeyValue> tags,
      GraphQLConfiguration configuration,
      String domain,
      Integer version,
      GraphQLNameDomain schema) {
    streamService.upsert(
        Stream
            .builder()
            .name(name)
            .owner("root") //TODO inject user
            .description(description)
            .tags(GraphQLKeyValue.toDto(tags))
            .configuration(GraphQLConfiguration.toDto(configuration))
            .domain(domain)
            .version(version)
            .schema(GraphQLNameDomain.toDto(schema))
            .build()
    );
    return true;
  }
}
