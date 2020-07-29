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
package com.expediagroup.streamplatform.streamregistry.state.graphql;

import static com.expediagroup.streamplatform.streamregistry.state.graphql.type.CustomType.OBJECTNODE;

import java.util.function.Consumer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;

import com.apollographql.apollo.ApolloClient;

@RequiredArgsConstructor
public class DefaultApolloClientFactory implements ApolloClientFactory {
  @NonNull private final String streamRegistryUrl;
  @NonNull private final Consumer<ApolloClient.Builder> configurer;
  private Credentials credentials;

  public DefaultApolloClientFactory(String streamRegistryUrl) {
    this(streamRegistryUrl, builder -> {});
  }

  public DefaultApolloClientFactory(String streamRegistryUrl, Credentials credentials) {
    this(streamRegistryUrl, builder -> {});
    this.credentials = credentials;
  }

  public DefaultApolloClientFactory(String streamRegistryUrl, Consumer<ApolloClient.Builder> configurer, Credentials credentials) {
    this(streamRegistryUrl, configurer);
    this.credentials = credentials;
  }

  @Override
  public ApolloClient create() {
    OkHttpClient okHttpClient;

    if (credentials != null) {
      okHttpClient = new OkHttpClient.Builder()
          .authenticator((route, response) -> response.request().newBuilder()
              .header("Authorization", credentials.basic())
              .build())
          .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
              .header("Authorization", credentials.basic())
              .build()))
          .build();
    } else {
      okHttpClient = new OkHttpClient.Builder().build();
    }

    var builder = builder().okHttpClient(okHttpClient);
    configurer.accept(builder);
    ApolloClient apolloClient = builder
        .serverUrl(streamRegistryUrl)
        .addCustomTypeAdapter(OBJECTNODE, new ObjectNodeTypeAdapter())
        .build();
    return apolloClient;
  }

  // for testing
  ApolloClient.Builder builder() {
    return ApolloClient.builder();
  }
}