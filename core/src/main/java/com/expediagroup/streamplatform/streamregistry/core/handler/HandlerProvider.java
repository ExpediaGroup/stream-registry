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
package com.expediagroup.streamplatform.streamregistry.core.handler;

import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.expediagroup.streamplatform.streamregistry.handler.Handler;
import com.expediagroup.streamplatform.streamregistry.model.Entity;

@RequiredArgsConstructor
public class HandlerProvider<T extends Entity<?>> {
  private final Map<String, Handler<T>> handlers;

  public Handler<T> get(String type) {
    return Optional
        .ofNullable(handlers.get(type))
        .orElseThrow(() -> new IllegalArgumentException("Unknown handler type '" + type + "'."));
  }
}
