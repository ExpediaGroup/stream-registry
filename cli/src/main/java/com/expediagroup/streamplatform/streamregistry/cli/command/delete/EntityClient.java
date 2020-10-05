/**
 * Copyright (C) 2018-2020 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.cli.command.delete;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.api.Query;

import picocli.CommandLine.Option;

import com.expediagroup.streamplatform.streamregistry.cli.graphql.ConsumerBindingQuery;
import com.expediagroup.streamplatform.streamregistry.cli.graphql.ConsumerQuery;
import com.expediagroup.streamplatform.streamregistry.cli.graphql.DomainQuery;
import com.expediagroup.streamplatform.streamregistry.cli.graphql.InfrastructureQuery;
import com.expediagroup.streamplatform.streamregistry.cli.graphql.ProducerBindingQuery;
import com.expediagroup.streamplatform.streamregistry.cli.graphql.ProducerQuery;
import com.expediagroup.streamplatform.streamregistry.cli.graphql.SchemaQuery;
import com.expediagroup.streamplatform.streamregistry.cli.graphql.StreamBindingQuery;
import com.expediagroup.streamplatform.streamregistry.cli.graphql.StreamQuery;
import com.expediagroup.streamplatform.streamregistry.cli.graphql.ZoneQuery;
import com.expediagroup.streamplatform.streamregistry.state.graphql.ApolloExecutor;
import com.expediagroup.streamplatform.streamregistry.state.graphql.Credentials;
import com.expediagroup.streamplatform.streamregistry.state.graphql.DefaultApolloClientFactory;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ConsumerBindingKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.InfrastructureKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ProducerBindingKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.StreamBindingKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.StreamKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ZoneKey;

@RequiredArgsConstructor
class EntityClient {
  private final ApolloExecutor executor;
  private final KeyConverter converter;

  @Value
  static class StreamKeyWithSchemaKey {
    StreamKey stream;
    SchemaKey schema;
  }

  List<DomainKey> getDomainKeys(String domain) {
    return execute(
        DomainQuery.builder()
            .domain(domain)
            .build())
        .getDomain().getByQuery().stream()
        .map(x -> converter.domainKey(x.getKey())).collect(toList());
  }

  List<SchemaKey> getSchemaKeys(String domain, String schema) {
    return execute(
        SchemaQuery.builder()
            .domain(domain)
            .schema(schema)
            .build())
        .getSchema().getByQuery().stream()
        .map(x -> converter.schemaKey(x.getKey())).collect(toList());
  }

  List<StreamKeyWithSchemaKey> getStreamKeyWithSchemaKeys(String domain, String stream, int version, String schemaDomain, String schema) {
    return execute(
        StreamQuery.builder()
            .domain(domain)
            .stream(stream)
            .version(version)
            .schemaDomain(schemaDomain)
            .schema(schema)
            .build())
        .getStream().getByQuery().stream()
        .map(x -> {
          var streamKey = converter.streamKey(x.getKey());
          var schemaKey = converter.schemaKey(x.getSchema().getKey());
          return new StreamKeyWithSchemaKey(streamKey, schemaKey);
        }).collect(toList());
  }

  List<ZoneKey> getZoneKeys(String zone) {
    return execute(
        ZoneQuery.builder()
            .zone(zone)
            .build())
        .getZone().getByQuery().stream()
        .map(x -> converter.zoneKey(x.getKey())).collect(toList());
  }

  List<InfrastructureKey> getInfrastructureKeys(String zone, String infrastructure) {
    return execute(
        InfrastructureQuery.builder()
            .zone(zone)
            .infrastructure(infrastructure)
            .build())
        .getInfrastructure().getByQuery().stream()
        .map(x -> converter.infrastructureKey(x.getKey())).collect(toList());
  }

  List<ProducerKey> getProducerKeys(String domain, String stream, int version, String zone, String producer) {
    return execute(
        ProducerQuery.builder()
            .domain(domain)
            .stream(stream)
            .version(version)
            .zone(zone)
            .producer(producer)
            .build())
        .getProducer().getByQuery().stream()
        .map(x -> converter.producerKey(x.getKey())).collect(toList());
  }

  List<ConsumerKey> getConsumerKeys(String domain, String stream, int version, String zone, String consumer) {
    return execute(
        ConsumerQuery.builder()
            .domain(domain)
            .stream(stream)
            .version(version)
            .zone(zone)
            .consumer(consumer)
            .build())
        .getConsumer().getByQuery().stream()
        .map(x -> converter.consumerKey(x.getKey())).collect(toList());
  }

  List<ProducerBindingKey> getProducerBindingKeys(String domain, String stream, int version, String zone, String infrastructure, String producer) {
    return execute(
        ProducerBindingQuery.builder()
            .domain(domain)
            .stream(stream)
            .version(version)
            .zone(zone)
            .infrastructure(infrastructure)
            .producer(producer)
            .build())
        .getProducerBinding().getByQuery().stream()
        .map(x -> converter.producerBindingKey(x.getKey())).collect(toList());
  }

  List<ConsumerBindingKey> getConsumerBindingKeys(String domain, String stream, int version, String zone, String infrastructure, String consumer) {
    return execute(
        ConsumerBindingQuery.builder()
            .domain(domain)
            .stream(stream)
            .version(version)
            .zone(zone)
            .infrastructure(infrastructure)
            .consumer(consumer)
            .build())
        .getConsumerBinding().getByQuery().stream()
        .map(x -> converter.consumerBindingKey(x.getKey())).collect(toList());
  }

  List<StreamBindingKey> getStreamBindingKeys(String domain, String stream, int version, String zone, String infrastructure) {
    return execute(
        StreamBindingQuery.builder()
            .domain(domain)
            .stream(stream)
            .version(version)
            .zone(zone)
            .infrastructure(infrastructure)
            .build())
        .getStreamBinding().getByQuery().stream()
        .map(x -> converter.streamBindingKey(x.getKey())).collect(toList());
  }

  private <D extends Operation.Data, T, V extends Operation.Variables, R, K> T execute(Query<D, Optional<T>, V> query) {
    return executor.execute(query).join().getData().get();
  }

  static class Factory {
    @Option(names = "--streamRegistryUrl", required = true) String streamRegistryUrl;
    @Option(names = "--streamRegistryUsername", required = false) String streamRegistryUsername;
    @Option(names = "--streamRegistryPassword", required = false) String streamRegistryPassword;

    EntityClient create() {
      ApolloClient client;
      if(streamRegistryUsername != null && streamRegistryPassword != null) {
        client = new DefaultApolloClientFactory(streamRegistryUrl, new Credentials(streamRegistryUsername, streamRegistryPassword)).create();
      } else {
        client = new DefaultApolloClientFactory(streamRegistryUrl).create();
      }
      ApolloExecutor executor = new ApolloExecutor(client);
      KeyConverter converter = new KeyConverter();
      return new EntityClient(executor, converter);
    }
  }
}
