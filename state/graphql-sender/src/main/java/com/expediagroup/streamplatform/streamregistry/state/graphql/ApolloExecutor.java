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

import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Operation.Data;
import com.apollographql.apollo.api.Operation.Variables;
import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.jetbrains.annotations.NotNull;

@Slf4j
@RequiredArgsConstructor
public class ApolloExecutor {
  @NonNull private final ApolloClient client;

  public <D extends Data, T, V extends Variables> CompletableFuture<Response<T>> execute(Mutation<D, T, V> mutation) {
    var future = new CompletableFuture<Response<T>>();
    client.mutate(mutation).enqueue(new Callback<>(future));
    return future;
  }

  public <D extends Data, T, V extends Variables> CompletableFuture<Response<T>> execute(Query<D, T, V> query) {
    var future = new CompletableFuture<Response<T>>();
    client.query(query).enqueue(new Callback<>(future));
    return future;
  }

  @RequiredArgsConstructor
  static class Callback<T> extends ApolloCall.Callback<T> {
    private final CompletableFuture<Response<T>> future;

    @Override
    public void onResponse(@NotNull Response<T> response) {
      if (response.hasErrors()) {
        List<com.apollographql.apollo.api.Error> errors = response.getErrors();
        future.completeExceptionally(new IllegalStateException("Unexpected response: " + errors.stream().map(e -> e.getMessage()).collect(joining(", "))));
      } else {
        future.complete(response);
      }
    }

    @Override
    public void onFailure(@NotNull ApolloException e) {
      log.error("Operation failed", e);
      future.completeExceptionally(e);
    }
  }
}
