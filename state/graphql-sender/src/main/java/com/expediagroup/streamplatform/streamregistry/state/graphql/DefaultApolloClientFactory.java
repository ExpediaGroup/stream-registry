package com.expediagroup.streamplatform.streamregistry.state.graphql;

import static com.expediagroup.streamplatform.streamregistry.state.graphql.type.CustomType.OBJECTNODE;

import java.util.function.Consumer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import com.apollographql.apollo.ApolloClient;

import okhttp3.OkHttpClient;

@RequiredArgsConstructor
public class DefaultApolloClientFactory implements ApolloClientFactory {
  @NonNull private final String streamRegistryUrl;
  @NonNull private final Consumer<ApolloClient.Builder> configurer;

  public DefaultApolloClientFactory(String streamRegistryUrl) {
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
