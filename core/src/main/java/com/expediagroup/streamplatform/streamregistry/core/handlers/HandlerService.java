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
package com.expediagroup.streamplatform.streamregistry.core.handlers;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.handler.Handler;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Entity;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Specified;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.Zone;

@Component
@Slf4j
public class HandlerService {
  private final Map<Key, Handler> handlers;

  public HandlerService(List<Handler> handlers) {
    validateHandlers(handlers);
    this.handlers = handlers
        .stream()
        .peek(h -> log.info("Loaded handler for {} - {}", h.target().getSimpleName(), h.type()))
        .collect(toMap(
            handler -> new Key(handler.type(), handler.target()),
            Function.identity()
        ));
  }

  private static void validateHandlers(List<Handler> handlers) {
    Set<Class> handlerTargets = handlers.stream().map(Handler::target).collect(toSet());
    check(handlerTargets, Domain.class);
    check(handlerTargets, Schema.class);
    check(handlerTargets, Stream.class);
    check(handlerTargets, Producer.class);
    check(handlerTargets, Consumer.class);
    check(handlerTargets, Zone.class);
    check(handlerTargets, Infrastructure.class);
    check(handlerTargets, StreamBinding.class);
    check(handlerTargets, ProducerBinding.class);
    check(handlerTargets, ConsumerBinding.class);
  }

  private static void check(Set<Class> handlerTargets, Class target) {
    checkArgument(handlerTargets.contains(target), "No Handlers for " + target.getSimpleName() + " defined");
  }

  private <T extends Entity> Handler<T> getHandler(T entity) {
    Handler<?> handler = handlers.get(new Key(entity.getSpecification().getType(), entity.getClass()));
    if (handler == null) {
      Set<String> supportedTypes = handlers.keySet().stream()
          .filter(k -> k.entityClass.equals(entity.getClass()))
          .map(Key::getType)
          .collect(toSet());
      throw new ValidationException(
          "There is no handler for " + entity.getClass().getSimpleName()
              + " entities with type " + entity.getSpecification().getType()
              + ". Expected one of " + supportedTypes);
    }
    return (Handler<T>) handler;
  }

  public <T extends Entity> Specification handleInsert(T entity) {
    try {
      return getHandler(entity).handleInsert(entity);
    } catch (Exception e) {
      throw new ValidationException(e);
    }
  }

  public Specification handleUpdate(Entity entity, Entity existing) {
    try {
      return getHandler(entity).handleUpdate(entity, existing);
    } catch (Exception e) {
      throw new ValidationException(e);
    }
  }

  @Value
  static class Key {
    String type;
    Class<? extends Specified> entityClass;
  }
}
