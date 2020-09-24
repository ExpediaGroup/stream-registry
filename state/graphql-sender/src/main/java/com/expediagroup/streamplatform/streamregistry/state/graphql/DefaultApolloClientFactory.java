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

import static com.expediagroup.streamplatform.streamregistry.state.graphql.security.X509TrustManagers.TRUST_ALL;
import static com.expediagroup.streamplatform.streamregistry.state.graphql.type.CustomType.OBJECTNODE;
import static okhttp3.Credentials.basic;

import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;

import com.apollographql.apollo.ApolloClient;

@RequiredArgsConstructor
@AllArgsConstructor
public class DefaultApolloClientFactory implements ApolloClientFactory {
  @NonNull private final String streamRegistryUrl;
  private Credentials credentials;
  private boolean disableHostnameVerification;
  private boolean disableCertificateVerification;

  @Override
  @SneakyThrows
  public ApolloClient create() {
    var okHttpClientBuilder = new OkHttpClient.Builder();
    if (credentials != null) {
      okHttpClientBuilder.addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
          .header("Authorization", basic(credentials.getUsername(), credentials.getPassword()))
          .build()));
    }

    if (disableHostnameVerification) {
      okHttpClientBuilder.hostnameVerifier((hostname, session) -> true);
    }

    if (disableCertificateVerification) {
      SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, new TrustManager[]{TRUST_ALL}, new SecureRandom());
      okHttpClientBuilder.sslSocketFactory(sslContext.getSocketFactory(), TRUST_ALL);
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
