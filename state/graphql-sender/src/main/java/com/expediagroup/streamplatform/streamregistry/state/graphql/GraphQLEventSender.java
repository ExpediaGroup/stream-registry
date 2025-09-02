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

import static lombok.AccessLevel.PACKAGE;

import java.util.concurrent.CompletableFuture;

import com.apollographql.apollo.ApolloClient;

import com.expediagroup.streamplatform.streamregistry.state.EventSender;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(access = PACKAGE)
public class GraphQLEventSender implements EventSender {
  @NonNull private final ApolloExecutor executor;
  @NonNull private final GraphQLConverter converter;

  public GraphQLEventSender(ApolloClient client) {
    this(new ApolloExecutor(client), new GraphQLConverter());
  }

  @Override
  public <K extends Entity.Key<S>, S extends Specification> CompletableFuture<Void> send(Event<K, S> event) {
    val mutation = converter.convert(event);
    return executor.execute(mutation).thenApply(x -> null);
  }

  @Override
  public void close() {}
}
