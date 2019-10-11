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
package com.expediagroup.streamplatform.streamregistry.graphql.client.reactor;

import static com.apollographql.apollo.ApolloCall.StatusEvent.COMPLETED;

import lombok.RequiredArgsConstructor;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloCall.StatusEvent;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.internal.subscription.ApolloSubscriptionTerminatedException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.FluxSink.OverflowStrategy;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

public final class ReactorApollo {
  private ReactorApollo() {}

  public static <T> Mono<Response<T>> from(ApolloCall<T> call) {
    return Mono.create(sink -> {
      sink.onCancel(call::cancel);
      call.enqueue(new MonoSinkCallback<>(sink));
    });
  }

  public static <T> Flux<Response<T>> from(ApolloSubscriptionCall<T> call, OverflowStrategy overflowStrategy) {
    return Flux.create(sink -> {
      sink.onCancel(call::cancel);
      call.execute(new FluxSinkCallback<>(sink));
    }, overflowStrategy);
  }

  @RequiredArgsConstructor
  static class MonoSinkCallback<T> extends ApolloCall.Callback<T> {
    private final MonoSink<Response<T>> sink;

    @Override
    public void onResponse(Response<T> response) {
      sink.success(response);
    }

    @Override
    public void onFailure(ApolloException e) {
      sink.error(e);
    }

    @Override
    public void onStatusEvent(StatusEvent event) {
      if (event == COMPLETED) {
        sink.success();
      }
    }
  }

  @RequiredArgsConstructor
  static class FluxSinkCallback<T> implements ApolloSubscriptionCall.Callback<T> {
    private final FluxSink<Response<T>> sink;

    @Override
    public void onResponse(Response<T> response) {
      if (!sink.isCancelled()) {
        sink.next(response);
      }
    }

    @Override
    public void onFailure(ApolloException e) {
      if (!sink.isCancelled()) {
        sink.error(e);
      }
    }

    public void onCompleted() {
      if (!sink.isCancelled()) {
        sink.complete();
      }
    }

    public void onTerminated() {
      onFailure(new ApolloSubscriptionTerminatedException("Subscription server unexpectedly terminated connection"));
    }

    @Override
    public void onConnected() {}
  }


}
