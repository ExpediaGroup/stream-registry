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

import java.util.concurrent.CompletableFuture;

public interface EventCorrelator {
  void register(String correlationId, CompletableFuture<Void> future);

  void received(String correlationId);

  void failed(String correlationId, Exception e);

  String CORRELATION_ID = "correlationId";

  EventCorrelator NULL = new EventCorrelator() {
    @Override
    public void register(String correlationId, CompletableFuture<Void> future) {}

    @Override
    public void received(String correlationId) {}

    @Override
    public void failed(String correlationId, Exception e) {}
  };
}
