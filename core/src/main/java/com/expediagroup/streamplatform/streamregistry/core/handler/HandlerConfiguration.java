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

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.expediagroup.streamplatform.streamregistry.handler.Handler;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Entity;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

@Configuration
class HandlerConfiguration {
  @Bean
  HandlerWrapper<Domain> domainHandlerProvider(List<Handler<Domain>> handlers) {
    checkArgument(handlers.size() > 0, "There must be at least one Domain Handler.");
    return new HandlerWrapper<>(new HandlerProvider<>(index(handlers)));
  }

  @Bean
  HandlerWrapper<Schema> schemaHandlerProvider(List<Handler<Schema>> handlers) {
    checkArgument(handlers.size() > 0, "There must be at least one Schema Handler.");
    return new HandlerWrapper<>(new HandlerProvider<>(index(handlers)));
  }

  @Bean
  HandlerWrapper<Stream> streamHandlerProvider(List<Handler<Stream>> handlers) {
    checkArgument(handlers.size() > 0, "There must be at least one Stream Handler.");
    return new HandlerWrapper<>(new HandlerProvider<>(index(handlers)));
  }

  private <T extends Entity<?>> Map<String, Handler<T>> index(List<Handler<T>> handlers) {
    return handlers
        .stream()
        .collect(toMap(Handler::type, identity()));
  }
}
