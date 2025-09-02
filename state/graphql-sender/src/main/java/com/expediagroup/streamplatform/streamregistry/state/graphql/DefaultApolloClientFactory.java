/**
 * Copyright (C) 2018-2025 Expedia, Inc.
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
import static okhttp3.Credentials.basic;

import java.util.function.Consumer;

import com.apollographql.apollo.ApolloClient;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;

@AllArgsConstructor
public class DefaultApolloClientFactory implements ApolloClientFactory {
  @NonNull private final String streamRegistryUrl;
  private Credentials credentials;
  @NonNull private final Consumer<OkHttpClient.Builder> configurer;

  public DefaultApolloClientFactory(String streamRegistryUrl) {
    this(streamRegistryUrl, null, builder -> {});
  }
  public DefaultApolloClientFactory(String streamRegistryUrl, Consumer<Builder> configurer) {
    this(streamRegistryUrl, null, configurer);
  }
  public DefaultApolloClientFactory(String streamRegistryUrl, Credentials credentials) {
    this(streamRegistryUrl, credentials, builder -> {});
  }

  @Override
  @SneakyThrows
  public ApolloClient create() {
    val okHttpClientBuilder = new OkHttpClient.Builder();
    configurer.accept(okHttpClientBuilder);

    if (credentials != null) {
      okHttpClientBuilder.addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
          .header("Authorization", basic(credentials.getUsername(), credentials.getPassword()))
          .build()));
    }

    return builder()
        .okHttpClient(okHttpClientBuilder.build())
        .serverUrl(streamRegistryUrl)
        .addCustomTypeAdapter(OBJECTNODE, new ObjectNodeTypeAdapter())
        .build();
  }

  // for testing
  ApolloClient.Builder builder() {
    return ApolloClient.builder();
  }
}
