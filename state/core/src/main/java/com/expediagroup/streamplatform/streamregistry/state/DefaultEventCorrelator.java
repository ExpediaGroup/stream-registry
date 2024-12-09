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
package com.expediagroup.streamplatform.streamregistry.state;

import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PACKAGE;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import com.expediagroup.streamplatform.streamregistry.state.internal.EventCorrelator;

@Slf4j
@RequiredArgsConstructor(access = PACKAGE)
public class DefaultEventCorrelator implements EventCorrelator {
  private final Map<String, CompletableFuture<Void>> futures;

  public DefaultEventCorrelator() {
    this(new ConcurrentHashMap<>());
  }

  @Override
  public String register(CompletableFuture<Void> future) {
    val correlationId = randomUUID().toString();
    futures.put(correlationId, future);
    log.debug("registered: {}", correlationId);
    return correlationId;
  }

  @Override
  public void received(String correlationId) {
    log.debug("received: {}", correlationId);
    remove(correlationId).ifPresent(future -> future.complete(null));
  }

  @Override
  public void failed(String correlationId, Exception e) {
    log.debug("failed: {}", correlationId);
    remove(correlationId).ifPresent(future -> future.completeExceptionally(e));
  }

  private Optional<CompletableFuture<Void>> remove(String correlationId) {
    return Optional.ofNullable(futures.remove(correlationId));
  }
}
