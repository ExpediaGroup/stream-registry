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

import static lombok.AccessLevel.PACKAGE;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PACKAGE)
public
class DefaultEventCorrelator implements EventCorrelator {
  private final Map<String, CompletableFuture<Void>> futures;

  DefaultEventCorrelator() {
    this(new ConcurrentHashMap<>());
  }

  @Override
  public void register(String correlationId, CompletableFuture<Void> future) {
    futures.put(correlationId, future);
  }

  @Override
  public void received(String correlationId) {
    var future = futures.remove(correlationId);
    if (future != null) {
      future.complete(null);
    }
  }

  @Override
  public void failed(String correlationId, Exception e) {
    var future = futures.remove(correlationId);
    if (future != null) {
      future.completeExceptionally(e);
    }
  }
}
