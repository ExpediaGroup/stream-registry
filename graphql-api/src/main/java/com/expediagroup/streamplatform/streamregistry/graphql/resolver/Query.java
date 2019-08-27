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

import static com.expediagroup.streamplatform.streamregistry.graphql.resolver.KeyConvertor.convert;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLConsumer;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLDomain;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLInfrastructure;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLProducer;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLProducerBinding;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLSchema;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLStream;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLStreamBinding;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLTransformer;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLZone;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.service.Service;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class Query implements GraphQLQueryResolver {
  private final Service<Domain, Domain.Key> domainService;
  private final Service<Schema, Schema.Key> schemaService;
  private final Service<Stream, Stream.Key> streamService;
  private final GraphQLTransformer transformer;

  public Query(
      Service<Domain, Domain.Key> domainService,
      Service<Schema, Schema.Key> schemaService,
      Service<Stream, Stream.Key> streamService) {
    this.domainService = domainService;
    this.schemaService = schemaService;
    this.streamService = streamService;
    this.transformer = new GraphQLTransformer();
  }

  public List<GraphQLDomain> domains(
      String name,
      String owner,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration) {
    return domainService
        .stream(Domain
            .builder()
            .name(name)
            .owner(owner)
            .description(description)
            .tags(tags)
            .type(type)
            .configuration(configuration)
            .build())
        .map(transformer::transform)
        .collect(toList());
  }

  public List<GraphQLSchema> schemas(
      String name,
      String owner,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      String domain) {
    return schemaService
        .stream(Schema
            .builder()
            .name(name)
            .owner(owner)
            .description(description)
            .tags(tags)
            .type(type)
            .configuration(configuration)
            .domainKey(new Domain.Key(domain))
            .build())
        .map(transformer::transform)
        .collect(toList());
  }

  public List<GraphQLStream> streams(
      String name,
      String owner,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      String domain,
      Integer version,
      GraphQLSchema.Key schema) {
    return streamService
        .stream(Stream
            .builder()
            .name(name)
            .owner(owner)
            .description(description)
            .tags(tags)
            .type(type)
            .configuration(configuration)
            .domainKey(new Domain.Key(domain))
            .version(version)
            .schemaKey(convert(schema))
            .build())
        .map(transformer::transform)
        .collect(toList());
  }

  public List<GraphQLZone> zones(
      String name,
      String owner,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration) {
    // TODO implement
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  public List<GraphQLInfrastructure> infrastructures(
      String name,
      String owner,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      String zone) {
    // TODO implement
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  public List<GraphQLProducer> producers(
      String name,
      String owner,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      GraphQLStream.Key stream,
      List<String> zones) {
    // TODO implement
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  public List<GraphQLConsumer> consumers(
      String name,
      String owner,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      GraphQLStream.Key stream,
      List<String> zones) {
    // TODO implement
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  public List<GraphQLStreamBinding> streamBindings(
      String name,
      String owner,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      GraphQLStream.Key stream,
      GraphQLInfrastructure.Key infrastructure) {
    // TODO implement
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  public List<GraphQLProducerBinding> producerBindings(
      String name,
      String owner,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      String consumer,
      List<String> zones) {
    // TODO implement
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  public List<GraphQLConsumerBinding> consumerBindings(
      String name,
      String owner,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      String producer,
      List<String> zones) {
    // TODO implement
    throw new UnsupportedOperationException("Not yet implemented.");
  }
}
