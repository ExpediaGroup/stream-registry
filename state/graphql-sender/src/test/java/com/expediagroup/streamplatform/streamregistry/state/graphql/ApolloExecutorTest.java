/**
 * Copyright (C) 2018-2021 Expedia, Inc.
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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import lombok.val;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloMutationCall;
import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.state.graphql.ApolloExecutor.Callback;

@RunWith(MockitoJUnitRunner.class)
public class ApolloExecutorTest {
  @Mock private ApolloClient client;
  @Mock private Mutation<?, ?, ?> mutation;
  @SuppressWarnings("rawtypes")
  @Mock
  private ApolloMutationCall mutationCall;
  @Mock private Response response;

  @InjectMocks ApolloExecutor underTest;

  @Test
  public void onResponseNoErrors() {
    when(client.mutate(mutation)).thenReturn(mutationCall);

    CompletableFuture<? extends Response<?>> result = underTest.execute(mutation);

    val captor = ArgumentCaptor.forClass(Callback.class);
    verify(client).mutate(mutation);
    verify(mutationCall).enqueue(captor.capture());
    val callback = captor.getValue();

    assertThat(result.isDone(), is(false));
    when(response.hasErrors()).thenReturn(false);
    callback.onResponse(response);
    assertThat(result.isDone(), is(true));
  }

  @Test
  public void onResponseErrors() {
    when(client.mutate(mutation)).thenReturn(mutationCall);

    CompletableFuture<? extends Response<?>> result = underTest.execute(mutation);

    val captor = ArgumentCaptor.forClass(Callback.class);
    verify(client).mutate(mutation);
    verify(mutationCall).enqueue(captor.capture());
    val callback = captor.getValue();

    assertThat(result.isDone(), is(false));
    when(response.hasErrors()).thenReturn(true);
    callback.onResponse(response);
    assertThat(result.isCompletedExceptionally(), is(true));
    try {
      result.get();
      fail("Expected exception");
    } catch (Exception ex) {
      assertThat(ex.getCause(), instanceOf(ApolloResponseException.class));
      assertThat(((ApolloResponseException)ex.getCause()).getResponse(), is(response));
    }
  }

  @Test
  public void onFailure() {
    when(client.mutate(mutation)).thenReturn(mutationCall);

    CompletableFuture<? extends Response<?>> result = underTest.execute(mutation);

    val captor = ArgumentCaptor.forClass(Callback.class);
    verify(client).mutate(mutation);
    verify(mutationCall).enqueue(captor.capture());
    Callback callback = captor.getValue();

    assertThat(result.isDone(), is(false));
    callback.onFailure(new ApolloException(""));
    assertThat(result.isCompletedExceptionally(), is(true));
  }
}
