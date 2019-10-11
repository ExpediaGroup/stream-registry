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

import java.util.HashMap;
import java.util.Map;

import com.expediagroup.streamplatform.streamregistry.core.services.ValidationException;
import com.expediagroup.streamplatform.streamregistry.handler.Handler;
import com.expediagroup.streamplatform.streamregistry.handler.HandlerException;
import com.expediagroup.streamplatform.streamregistry.model.ManagedType;
import com.expediagroup.streamplatform.streamregistry.model.Specification;

public class HandlersForServices {

  private Map<String, Map<String, Handler>> map = new HashMap<>();

  public void register(String typeName, Class clazz, Handler handler) {
    Map<String, Handler> m = map.computeIfAbsent(typeName, h -> new HashMap<>());
    m.putIfAbsent(clazz.getCanonicalName(), handler);
  }

  private Handler get(ManagedType managedType) {
    if (!map.containsKey(managedType.getSpecification().getType())) {
      throw new ValidationException(managedType.getSpecification().getType() + " has no handlers, requires one of " + getTypes());
    }
    try {
      return map.get(managedType.getSpecification().getType()).get(managedType.getClass().getCanonicalName());
    } catch (NullPointerException e) {
    }
    return null;
  }

  public Specification handleInsert(ManagedType managedType) {
    Handler handler = get(managedType);
    if (handler == null) {
      return managedType.getSpecification();
    }
    try {
      return get(managedType).handleInsert(managedType);
    } catch (HandlerException e) {
      throw new ValidationException(e);
    }
  }

  public Specification handleUpdate(ManagedType managedType, ManagedType existing) {
    Handler handler = get(managedType);
    if (handler == null) {
      return managedType.getSpecification();
    }
    try {
      return get(managedType).handleUpdate(managedType, existing);
    } catch (HandlerException e) {
      throw new ValidationException(e);
    }
  }

  public Iterable<String> getTypes() {
    return map.keySet();
  }
}
