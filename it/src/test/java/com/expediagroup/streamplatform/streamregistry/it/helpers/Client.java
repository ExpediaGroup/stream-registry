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
package com.expediagroup.streamplatform.streamregistry.it.helpers;

import java.util.Optional;

import okhttp3.OkHttpClient;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Response;

import com.expediagroup.streamplatform.streamregistry.graphql.client.ConsumerBindingQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ConsumerQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.DomainQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InfrastructureQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.LongTypeAdapter;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ObjectNodeTypeAdapter;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ProducerBindingQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ProducerQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.SchemaQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.StreamBindingQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.StreamQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ZoneQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.reactor.ReactorApollo;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ConsumerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ConsumerKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.CustomType;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.DomainKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.InfrastructureKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ProducerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ProducerKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.SchemaKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.StreamBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.StreamKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ZoneKeyInput;

public class Client {

  ApolloClient apollo;

  public Client(String url) {
    apollo = ApolloClient
        .builder()
        .serverUrl(url)
        .okHttpClient(new OkHttpClient.Builder().build())
        .addCustomTypeAdapter(CustomType.OBJECTNODE, new ObjectNodeTypeAdapter())
        .addCustomTypeAdapter(CustomType.LONG, new LongTypeAdapter())
        .build();
  }

  public Response invoke(Operation operation) {
    Response response;
    if (operation instanceof Mutation) {
      response = (Response) ReactorApollo.from(apollo.mutate((Mutation) operation)).block();
    } else {
      response = (Response) ReactorApollo.from(apollo.query((Query) operation)).block();
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

  public void createZone(ITestDataFactory factory) {
    invoke(factory.upsertZoneMutationBuilder().build());
  }

  public void createDomain(ITestDataFactory factory) {
    createZone(factory);
    invoke(factory.upsertDomainMutationBuilder().build());
  }

  public void createSchema(ITestDataFactory factory) {
    createDomain(factory);
    invoke(factory.upsertSchemaMutationBuilder().build());
  }

  public void createStream(ITestDataFactory factory) {
    createSchema(factory);
    invoke(factory.upsertStreamMutationBuilder().build());
  }

  public void createProducer(ITestDataFactory factory) {
    createStream(factory);
    invoke(factory.upsertProducerMutationBuilder().build());
  }

  public void createProducerBinding(ITestDataFactory factory) {
    createStream(factory);
    invoke(factory.upsertProducerBindingMutationBuilder().build());
  }

  public void createConsumer(ITestDataFactory factory) {
    createStream(factory);
    invoke(factory.upsertConsumerMutationBuilder().build());
  }

  public void createConsumerBinding(ITestDataFactory factory) {
    createStream(factory);
    invoke((factory.upsertConsumerBindingMutationBuilder().build()));
  }
}
