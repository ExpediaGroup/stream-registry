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

import java.util.concurrent.CompletableFuture;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class ApolloExecutor {
  @NonNull private final ApolloClient client;

  public CompletableFuture<Void> execute(Mutation<?, ?, ?> mutation) {
    var future = new CompletableFuture<Void>();
    client.mutate(mutation).enqueue(new Callback(future));
    return future;
  }

  @RequiredArgsConstructor
  public class Callback extends ApolloCall.Callback {
    private final CompletableFuture<Void> future;

    @Override
    public void onResponse(@NotNull Response response) {
      if (!response.errors().isEmpty()) {
        future.completeExceptionally(new IllegalStateException("Unexpected response: " + response.errors()));
      } else {
        future.complete(null);
      }
    }

    @Override
    public void onFailure(@NotNull ApolloException e) {
      future.completeExceptionally(e);
    }
  }
}
