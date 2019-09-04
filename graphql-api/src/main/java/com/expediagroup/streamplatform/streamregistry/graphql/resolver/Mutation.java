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

import static com.expediagroup.streamplatform.streamregistry.graphql.resolver.KeyConvertor.convert;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLInfrastructure;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLSchema;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLStream;
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

  public boolean createDomain(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration) {
    return upsertDomain(name, description, tags, type, configuration);
  }

  public boolean updateDomain(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration) {
    return upsertDomain(name, description, tags, type, configuration);
  }

  private boolean upsertDomain(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration) {
    domainService.upsert(
        Domain
            .builder()
            .name(name)
            .owner("root") //TODO inject user
            .description(description)
            .tags(tags)
            .type(type)
            .configuration(configuration)
            .build()
    );
    return true;
  }

  public boolean createSchema(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      String domain) {
    return upsertSchema(name, description, tags, type, configuration, domain);
  }

  public boolean updateSchema(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      String domain) {
    return upsertSchema(name, description, tags, type, configuration, domain);
  }

  private boolean upsertSchema(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      String domain) {
    schemaService.upsert(
        Schema
            .builder()
            .name(name)
            .owner("root") //TODO inject user
            .description(description)
            .tags(tags)
            .type(type)
            .configuration(configuration)
            .domainKey(new Domain.Key(domain))
            .build()
    );
    return true;
  }

  public boolean createStream(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      String domain,
      Integer version,
      GraphQLSchema.Key schema) {
    return upsertStream(name, description, tags, type, configuration, domain, version, schema);
  }

  public boolean updateStream(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      String domain,
      Integer version,
      GraphQLSchema.Key schema) {
    return upsertStream(name, description, tags, type, configuration, domain, version, schema);
  }

  private boolean upsertStream(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      String domain,
      Integer version,
      GraphQLSchema.Key schema) {
    streamService.upsert(
        Stream
            .builder()
            .name(name)
            .owner("root") //TODO inject user
            .description(description)
            .tags(tags)
            .type(type)
            .configuration(configuration)
            .domainKey(new Domain.Key(domain))
            .version(version)
            .schemaKey(convert(schema))
            .build()
    );
    return true;
  }

  public boolean createZone(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration) {
    return updateZone(name, description, tags, type, configuration);
  }

  public boolean updateZone(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration) {
    return updateZone(name, description, tags, type, configuration);
  }

  private boolean upsertZone(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration) {
    // TODO implement
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  public boolean createInfrastructure(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      String zone) {
    return upsertInfrastructure(name, description, tags, type, configuration, zone);
  }

  public boolean updateInfrastructure(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      String zone) {
    return upsertInfrastructure(name, description, tags, type, configuration, zone);
  }

  private boolean upsertInfrastructure(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      String zone) {
    // TODO implement
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  public boolean createProducer(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      GraphQLStream.Key stream,
      List<String> zones) {
    return upsertProducer(name, description, tags, type, configuration, stream, zones);
  }

  public boolean updateProducer(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      GraphQLStream.Key stream,
      List<String> zones) {
    return upsertProducer(name, description, tags, type, configuration, stream, zones);
  }

  private boolean upsertProducer(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      GraphQLStream.Key stream,
      List<String> zones) {
    // TODO implement
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  public boolean createConsumer(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      GraphQLStream.Key stream,
      List<String> zones) {
    return upsertConsumer(name, description, tags, type, configuration, stream, zones);
  }

  public boolean updateConsumer(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      GraphQLStream.Key stream,
      List<String> zones) {
    return upsertConsumer(name, description, tags, type, configuration, stream, zones);
  }

  private boolean upsertConsumer(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      GraphQLStream.Key stream,
      List<String> zones) {
    // TODO implement
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  public boolean createStreamBinding(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      GraphQLStream.Key stream,
      GraphQLInfrastructure.Key infrastructure) {
    return upsertStreamBinding(name, description, tags, type, configuration, stream, infrastructure);
  }

  public boolean updateStreamBinding(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      GraphQLStream.Key stream,
      GraphQLInfrastructure.Key infrastructure) {
    return upsertStreamBinding(name, description, tags, type, configuration, stream, infrastructure);
  }

  private boolean upsertStreamBinding(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      GraphQLStream.Key stream,
      GraphQLInfrastructure.Key infrastructure) {
    // TODO implement
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  public boolean createProducerBinding(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      String producer,
      List<String> zones) {
    return upsertProducerBinding(name, description, tags, type, configuration, producer, zones);
  }

  public boolean updateProducerBinding(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      String producer,
      List<String> zones) {
    return upsertProducerBinding(name, description, tags, type, configuration, producer, zones);
  }

  private boolean upsertProducerBinding(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      String producer,
      List<String> zones) {
    // TODO implement
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  public boolean createConsumerBinding(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      String consumer,
      List<String> zones) {
    return upsertConsumerBinding(name, description, tags, type, configuration, consumer, zones);
  }

  public boolean updateConsumerBinding(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      String consumer,
      List<String> zones) {
    return upsertConsumerBinding(name, description, tags, type, configuration, consumer, zones);
  }

  private boolean upsertConsumerBinding(
      String name,
      String description,
      Map<String, String> tags,
      String type,
      ObjectNode configuration,
      String consumer,
      List<String> zones) {
    // TODO implement
    throw new UnsupportedOperationException("Not yet implemented.");
  }
}
