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
package com.expediagroup.streamplatform.streamregistry.it;

import java.util.Optional;

import okhttp3.OkHttpClient;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Response;

import com.expediagroup.streamplatform.streamregistry.graphql.client.DomainQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ObjectNodeTypeAdapter;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertDomainMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.reactor.ReactorApollo;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.CustomType;

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

  public Response invoke(Operation operation){
    if(operation instanceof Mutation){
      return mutate((Mutation)operation);
    }
    return query((Query) operation);
  }

  public Object data(Operation operation){
    return invoke(operation).data();
  }

  public Response mutate(Mutation mutation){
    return (Response)ReactorApollo.from(apollo.mutate(mutation)).block();
  }

  public Response query(Query query){
    return (Response)ReactorApollo.from(apollo.query(query)).block();
  }

  public UpsertDomainMutation.Upsert domainUpsert(Operation operation){
    return  ((Optional<UpsertDomainMutation.Data>) invoke(operation).data()).get().getDomain().getUpsert();
  }

  public DomainQuery.Domain domainQuery(Operation operation){
    return  ((Optional<DomainQuery.Data>) invoke(operation).data()).get().getDomain();
  }


}
