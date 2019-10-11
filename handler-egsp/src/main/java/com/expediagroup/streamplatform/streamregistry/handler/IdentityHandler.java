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
package com.expediagroup.streamplatform.streamregistry.handler;

import lombok.RequiredArgsConstructor;

import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Specified;

@RequiredArgsConstructor
public class IdentityHandler<T extends Specified> implements Handler<T> {
  private final String type;
  private final Class<T> target;

  @Override
  public String type() {
    return type;
  }

  @Override
  public Class target() {
    return target;
  }

  @Override
  public Specification handleInsert(T entity) throws HandlerException {
    return entity.getSpecification();
  }

  @Override
  public Specification handleUpdate(T entity, T existing) throws HandlerException {
    return entity.getSpecification();
  }
}
