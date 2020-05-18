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
package com.expediagroup.streamplatform.streamregistry.state;

import static com.expediagroup.streamplatform.streamregistry.state.EventCorrelator.CORRELATION_ID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

public class DefaultEventCorrelatorTest {
  private final Map<String, CompletableFuture<Void>> futures = new ConcurrentHashMap<>();
  private final DefaultEventCorrelator underTest = new DefaultEventCorrelator(futures);

  private final CompletableFuture<Void> future = new CompletableFuture<>();

  @Test
  public void register() {
    underTest.register(CORRELATION_ID, future);

    assertThat(futures.get(CORRELATION_ID), is(future));
  }

  @Test
  public void received() {
    underTest.register(CORRELATION_ID, future);
    underTest.received(CORRELATION_ID);

    assertThat(futures.isEmpty(), is(true));
    assertThat(future.isDone(), is(true));
  }

  @Test
  public void failed() {
    underTest.register(CORRELATION_ID, future);
    underTest.failed(CORRELATION_ID, new Exception());

    assertThat(futures.isEmpty(), is(true));
    assertThat(future.isCompletedExceptionally(), is(true));
  }

}
