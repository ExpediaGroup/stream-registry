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

import static java.util.stream.Collectors.toList;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLConfiguration;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLDomain;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLKeyValue;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLNameDomain;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLSchema;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLStream;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.service.Service;

@Component
@RequiredArgsConstructor
public class Query implements GraphQLQueryResolver {
  private final Service<Domain, Domain.Key> domainService;
  private final Service<Schema, Schema.Key> schemaService;
  private final Service<Stream, Stream.Key> streamService;

  public List<GraphQLDomain> domains(
      String name,
      String owner,
      String description,
      Iterable<GraphQLKeyValue> tags) {
    return domainService
        .stream(Domain
            .builder()
            .name(name)
            .owner(owner)
            .description(description)
            .tags(GraphQLKeyValue.toDto(tags))
            .build())
        .map(GraphQLDomain::fromDto)
        .collect(toList());
  }

  public List<GraphQLSchema> schemas(
      String name,
      String owner,
      String description,
      Iterable<GraphQLKeyValue> tags,
      GraphQLConfiguration configuration,
      String domain) {
    System.out.println(Schema
        .builder()
        .name(name)
        .owner(owner)
        .description(description)
        .tags(GraphQLKeyValue.toDto(tags))
        .configuration(GraphQLConfiguration.toDto(configuration))
        .domain(domain)
        .build());
    return schemaService
        .stream(Schema
            .builder()
            .name(name)
            .owner(owner)
            .description(description)
            .tags(GraphQLKeyValue.toDto(tags))
            .configuration(GraphQLConfiguration.toDto(configuration))
            .domain(domain)
            .build())
        .map(schema -> GraphQLSchema.fromDto(schema, domain(schema.getDomain())))
        .collect(toList());
  }

  public List<GraphQLStream> streams(
      String name,
      String owner,
      String description,
      Iterable<GraphQLKeyValue> tags,
      GraphQLConfiguration configuration,
      String domain,
      Integer version,
      GraphQLNameDomain schema) {
    return streamService
        .stream(Stream
            .builder()
            .name(name)
            .owner(owner)
            .description(description)
            .tags(GraphQLKeyValue.toDto(tags))
            .configuration(GraphQLConfiguration.toDto(configuration))
            .domain(domain)
            .version(version)
            .schema(GraphQLNameDomain.toDto(schema))
            .build())
        .map(stream -> {
          Schema streamSchema = schema(stream.getSchema().getName(), stream.getSchema().getDomain());
          return GraphQLStream.fromDto(
              stream,
              domain(stream.getDomain()),
              streamSchema,
              domain(streamSchema.getDomain())
          );
        })
        .collect(toList());
  }

  private Domain domain(String name) {
    return domainService.get(new Domain.Key(name));
  }

  private Schema schema(String name, String domain) {
    return schemaService.get(new Schema.Key(name, domain));
  }
}
