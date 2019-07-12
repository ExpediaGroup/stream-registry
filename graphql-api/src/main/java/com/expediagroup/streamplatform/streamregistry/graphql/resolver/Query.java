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

import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLDomain;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLKeyValue;
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
      Iterable<GraphQLKeyValue> tags,
      String type,
      Iterable<GraphQLKeyValue> configuration) {
    return domainService
        .stream(Domain
            .builder()
            .name(name)
            .owner(owner)
            .description(description)
            .tags(GraphQLKeyValue.toDto(tags))
            .type(type)
            .configuration(GraphQLKeyValue.toDto(configuration))
            .build())
        .map(GraphQLDomain::fromDto)
        .collect(toList());
  }

  public List<GraphQLSchema> schemas(
      String name,
      String owner,
      String description,
      Iterable<GraphQLKeyValue> tags,
      String type,
      Iterable<GraphQLKeyValue> configuration,
      String domain) {
    return schemaService
        .stream(Schema
            .builder()
            .name(name)
            .owner(owner)
            .description(description)
            .tags(GraphQLKeyValue.toDto(tags))
            .type(type)
            .configuration(GraphQLKeyValue.toDto(configuration))
            .domain(Domain.Key
                .builder()
                .name(domain)
                .build())
            .build())
        .map(schema -> GraphQLSchema.fromDto(schema, domainService.get(schema.getDomain())))
        .collect(toList());
  }

  public List<GraphQLStream> streams(
      String name,
      String owner,
      String description,
      Iterable<GraphQLKeyValue> tags,
      String type,
      Iterable<GraphQLKeyValue> configuration,
      String domain,
      Integer version,
      GraphQLSchema.Key schema) {
    return streamService
        .stream(Stream
            .builder()
            .name(name)
            .owner(owner)
            .description(description)
            .tags(GraphQLKeyValue.toDto(tags))
            .type(type)
            .configuration(GraphQLKeyValue.toDto(configuration))
            .domain(Domain.Key
                .builder()
                .name(domain)
                .build())
            .version(version)
            .schema(Schema.Key
                .builder()
                .name(schema.getName())
                .domain(Domain.Key
                    .builder()
                    .name(schema.getDomain())
                    .build())
                .build())
            .build())
        .map(stream -> {
          Schema streamSchema = schemaService.get(stream.getSchema());
          return GraphQLStream.fromDto(
              stream,
              domainService.get(stream.getDomain()),
              streamSchema,
              domainService.get(streamSchema.getDomain())
          );
        })
        .collect(toList());
  }
}
