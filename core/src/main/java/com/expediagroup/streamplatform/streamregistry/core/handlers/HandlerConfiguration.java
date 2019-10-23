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
package com.expediagroup.streamplatform.streamregistry.core.handlers;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.expediagroup.streamplatform.streamregistry.handler.Handler;
import com.expediagroup.streamplatform.streamregistry.model.ManagedType;

@Configuration
class HandlerConfiguration {
  @Bean
  HandlersForServices handlersForServicesProvider(List<Handler> handlers) {
    HandlersForServices handlerRegistry = new HandlersForServices();
    for (Handler handler : handlers) {
      handlerRegistry.register(handler.type(), handler.target(), handler);
    }
    return handlerRegistry;
  }

  private <T extends ManagedType> Map<String, Handler<T>> index(List<Handler> handlers) {
    return handlers
        .stream()
        .collect(toMap(Handler::type, identity()));
  }
}
