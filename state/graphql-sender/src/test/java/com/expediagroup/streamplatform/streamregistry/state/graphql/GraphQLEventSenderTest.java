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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;

@RunWith(MockitoJUnitRunner.class)
public class GraphQLEventSenderTest {
  @Mock private ApolloExecutor executor;
  @Mock private GraphQLConverter converter;
  @Mock private Event<?, ?> event;
  @SuppressWarnings("rawtypes")
  @Mock
  private Mutation mutation;
  @Mock private CompletableFuture<Response> responseFuture;
  @Mock private CompletableFuture<Void> voidFuture;

  @InjectMocks private GraphQLEventSender underTest;

  @Test
  public void test() {
    when(converter.convert(event)).thenReturn(mutation);
    when(executor.execute(mutation)).thenReturn(responseFuture);
    Function<Response, Void> any = any();
    when(responseFuture.thenApply(any)).thenReturn(voidFuture);

    var result = underTest.send(event);

    assertThat(result, is(voidFuture));
  }
}
