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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static reactor.core.publisher.FluxSink.OverflowStrategy.BUFFER;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.internal.subscription.ApolloSubscriptionTerminatedException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import reactor.core.publisher.FluxSink;
import reactor.core.publisher.MonoSink;

import com.expediagroup.streamplatform.streamregistry.graphql.client.reactor.ReactorApollo.FluxSinkCallback;
import com.expediagroup.streamplatform.streamregistry.graphql.client.reactor.ReactorApollo.MonoSinkCallback;

@RunWith(MockitoJUnitRunner.class)
public class ReactorApolloTest {
  private final Operation<?, ?, ?> operation = mock(Operation.class);
  private final Response<String> response = Response.<String>builder(operation).build();
  private final ApolloException exception = new ApolloException(null);
  @Mock
  private ApolloCall<String> call;
  @Mock
  private ApolloSubscriptionCall<String> subscriptionCall;
  @Mock
  private MonoSink<Response<String>> monoSink;
  @Mock
  private FluxSink<Response<String>> fluxSink;

  @Test
  public void monoCancel() {
    ReactorApollo.from(call).subscribe().dispose();

    verify(call).cancel();
  }

  @Test
  public void monoSinkCallbackSuccess() {
    MonoSinkCallback<String> callback = new MonoSinkCallback<>(monoSink);
    callback.onResponse(response);

    verify(monoSink).success(response);
  }

  @Test
  public void monoSinkCallbackSuccessEmpty() {
    MonoSinkCallback<String> callback = new MonoSinkCallback<>(monoSink);
    callback.onStatusEvent(COMPLETED);

    verify(monoSink).success();
  }

  @Test
  public void monoSinkCallbackFailure() {
    MonoSinkCallback<String> callback = new MonoSinkCallback<>(monoSink);
    callback.onFailure(exception);

    verify(monoSink).error(exception);
  }

  @Test
  public void fluxCancel() {
    ReactorApollo.from(subscriptionCall, BUFFER).subscribe().dispose();

    verify(subscriptionCall).cancel();
  }

  @Test
  public void fluxSinkCallbackNext() {
    FluxSinkCallback<String> callback = new FluxSinkCallback<>(fluxSink);
    callback.onResponse(response);

    verify(fluxSink).next(response);
  }

  @Test
  public void fluxSinkCallbackCompleted() {
    FluxSinkCallback<String> callback = new FluxSinkCallback<>(fluxSink);
    callback.onCompleted();

    verify(fluxSink).complete();
  }

  @Test
  public void fluxSinkCallbackFailure() {
    FluxSinkCallback<String> callback = new FluxSinkCallback<>(fluxSink);
    callback.onFailure(exception);

    verify(fluxSink).error(exception);
  }

  @Test
  public void fluxSinkCallbackTerminated() {
    FluxSinkCallback<String> callback = new FluxSinkCallback<>(fluxSink);
    callback.onTerminated();

    verify(fluxSink).error(any(ApolloSubscriptionTerminatedException.class));
  }
}
