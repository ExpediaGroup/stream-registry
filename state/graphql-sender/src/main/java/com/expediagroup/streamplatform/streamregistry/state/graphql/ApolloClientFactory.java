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

import com.apollographql.apollo.ApolloClient;

import okhttp3.OkHttpClient;

public interface ApolloClientFactory {
  ApolloClient create();

  @RequiredArgsConstructor
  class DefaultApolloClientFactory implements ApolloClientFactory {
    @NonNull private final String streamRegistryUrl;
    @NonNull private final Consumer<ApolloClient.Builder> configurer;

    DefaultApolloClientFactory(String streamRegistryUrl) {
      this(streamRegistryUrl, builder -> {});
    }

    @Override
    public ApolloClient create() {
      var builder = builder()
          .okHttpClient(new OkHttpClient.Builder().build());
      configurer.accept(builder);
      return builder
          .serverUrl(streamRegistryUrl)
          .addCustomTypeAdapter(OBJECTNODE, new ObjectNodeTypeAdapter())
          .build();
    }

    // for testing
    ApolloClient.Builder builder() {
      return ApolloClient.builder();
    }
  }
}
