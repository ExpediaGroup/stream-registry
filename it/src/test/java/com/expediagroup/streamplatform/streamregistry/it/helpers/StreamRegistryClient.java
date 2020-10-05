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
package com.expediagroup.streamplatform.streamregistry.it.helpers;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Response;

import com.expediagroup.streamplatform.streamregistry.graphql.client.test.ConsumerBindingQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.ConsumerQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.DomainQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.InfrastructureQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.ProducerBindingQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.ProducerQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.SchemaQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.StreamBindingQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.StreamQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.ZoneQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.ConsumerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.ConsumerKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.DomainKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.InfrastructureKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.ProducerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.ProducerKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.SchemaKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.StreamBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.StreamKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.ZoneKeyInput;
import com.expediagroup.streamplatform.streamregistry.state.graphql.ApolloExecutor;
import com.expediagroup.streamplatform.streamregistry.state.graphql.Credentials;
import com.expediagroup.streamplatform.streamregistry.state.graphql.DefaultApolloClientFactory;

@RequiredArgsConstructor
public class StreamRegistryClient {
  private final ApolloExecutor executor;

  public StreamRegistryClient(final String url, final String username, final String password) {
    this(new DefaultApolloClientFactory(url, new Credentials(username, password)).create());
  }

  public StreamRegistryClient(ApolloClient apolloClient) {
    this(new ApolloExecutor(apolloClient));
  }

  public Response invoke(Operation operation) {
    Response response;
    if (operation instanceof Mutation) {
      response = (Response) executor.execute((Mutation) operation).join();
    } else {
      response = (Response) executor.execute((Query) operation).join();
    }
    if (response.hasErrors()) {
      throw new RuntimeException(((com.apollographql.apollo.api.Error) response.errors().get(0)).message());
    }
    return response;
  }

  public Optional<Object> getOptionalData(Operation operation) {
    Object out = invoke(operation).data();
    if (out instanceof Optional && ((Optional) out).isPresent()) {
      return (Optional) out;
    }
    return Optional.ofNullable(out);
  }

  public Optional<ZoneQuery.ByKey> getZone(ZoneKeyInput zoneKeyInput) {
    ZoneQuery.Data response = (ZoneQuery.Data) getOptionalData(ZoneQuery.builder().key(zoneKeyInput).build()).get();
    return response.getZone().getByKey();
  }

  public Optional<DomainQuery.ByKey> getDomain(DomainKeyInput zoneKeyInput) {
    DomainQuery.Data response = (DomainQuery.Data) getOptionalData(DomainQuery.builder().key(zoneKeyInput).build()).get();
    return response.getDomain().getByKey();
  }

  public Optional<InfrastructureQuery.ByKey> getInfrastructure(InfrastructureKeyInput zoneKeyInput) {
    InfrastructureQuery.Data response = (InfrastructureQuery.Data) getOptionalData(InfrastructureQuery.builder().key(zoneKeyInput).build()).get();
    return response.getInfrastructure().getByKey();
  }

  public Optional<ConsumerQuery.ByKey> getConsumer(ConsumerKeyInput zoneKeyInput) {
    ConsumerQuery.Data response = (ConsumerQuery.Data) getOptionalData(ConsumerQuery.builder().key(zoneKeyInput).build()).get();
    return response.getConsumer().getByKey();
  }

  public Optional<ConsumerBindingQuery.ByKey> getConsumerBinding(ConsumerBindingKeyInput zoneKeyInput) {
    ConsumerBindingQuery.Data response = (ConsumerBindingQuery.Data) getOptionalData(ConsumerBindingQuery.builder().key(zoneKeyInput).build()).get();
    return response.getConsumerBinding().getByKey();
  }

  public Optional<StreamBindingQuery.ByKey> getStreamBinding(StreamBindingKeyInput zoneKeyInput) {
    StreamBindingQuery.Data response = (StreamBindingQuery.Data) getOptionalData(StreamBindingQuery.builder().key(zoneKeyInput).build()).get();
    return response.getStreamBinding().getByKey();
  }

  public Optional<ProducerBindingQuery.ByKey> getProducerBinding(ProducerBindingKeyInput zoneKeyInput) {
    ProducerBindingQuery.Data response = (ProducerBindingQuery.Data) getOptionalData(ProducerBindingQuery.builder().key(zoneKeyInput).build()).get();
    return response.getProducerBinding().getByKey();
  }

  public Optional<ProducerQuery.ByKey> getProducer(ProducerKeyInput zoneKeyInput) {
    ProducerQuery.Data response = (ProducerQuery.Data) getOptionalData(ProducerQuery.builder().key(zoneKeyInput).build()).get();
    return response.getProducer().getByKey();
  }

  public Optional<StreamQuery.ByKey> getStream(StreamKeyInput zoneKeyInput) {
    StreamQuery.Data response = (StreamQuery.Data) getOptionalData(StreamQuery.builder().key(zoneKeyInput).build()).get();
    return response.getStream().getByKey();
  }

  public Optional<SchemaQuery.ByKey> getSchema(SchemaKeyInput zoneKeyInput) {
    SchemaQuery.Data response = (SchemaQuery.Data) getOptionalData(SchemaQuery.builder().key(zoneKeyInput).build()).get();
    return response.getSchema().getByKey();
  }
}
