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

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Response;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ObjectNodeTypeAdapter;
import com.expediagroup.streamplatform.streamregistry.graphql.client.reactor.ReactorApollo;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.CustomType;

import okhttp3.OkHttpClient;

public class Client {

  ApolloClient apollo;

  public Client(String url) {
    apollo = ApolloClient
        .builder()
        .serverUrl(url)
        .okHttpClient(new OkHttpClient.Builder().build())
        .addCustomTypeAdapter(CustomType.OBJECTNODE, new ObjectNodeTypeAdapter())
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

  public Object getData(Operation operation) {
    Object out = invoke(operation).data();
    if (out instanceof Optional && ((Optional) out).isPresent()) {
      return ((Optional) out).get();
    }
    return out;
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
}
