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

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Value;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.ValidationException;
import com.expediagroup.streamplatform.streamregistry.handler.Handler;
import com.expediagroup.streamplatform.streamregistry.handler.HandlerException;
import com.expediagroup.streamplatform.streamregistry.model.ManagedType;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Specified;

@Component
public class HandlerService {
  private final Map<Key, Handler<?>> handlers;

  public HandlerService(List<Handler> handlers) {
    this.handlers = handlers
        .stream()
        .collect(toMap(
            handler -> new Key(handler.type(), handler.target()),
            Function.identity()
        ));
  }

  private <T extends ManagedType> Handler<T> getHandler(T managedType) {
    Handler<?> handler = handlers.get(new Key(managedType.getSpecification().getType(), managedType.getClass()));
    if (handler == null) {
      Set<String> supportedTypes = handlers.keySet().stream()
          .filter(k -> k.entityClass.equals(managedType.getClass()))
          .map(Key::getType)
          .collect(toSet());
      throw new ValidationException(
          "There is no handler for " + managedType.getClass().getName()
              + " entities with type " + managedType.getSpecification().getType()
              + ". Expected one of " + supportedTypes);
    }
    return (Handler<T>) handler;
  }

  public <T extends ManagedType> Specification handleInsert(T managedType) {
    try {
      return getHandler(managedType).handleInsert(managedType);
    } catch (HandlerException e) {
      throw new ValidationException(e);
    }
  }

  public Specification handleUpdate(ManagedType managedType, ManagedType existing) {
    try {
      return getHandler(managedType).handleUpdate(managedType, existing);
    } catch (HandlerException e) {
      throw new ValidationException(e);
    }
  }

  @Value
  static class Key {
    String type;
    Class<? extends Specified> entityClass;
  }
}
