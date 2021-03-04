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
package com.expediagroup.streamplatform.streamregistry.core.handlers;

import lombok.RequiredArgsConstructor;

import com.expediagroup.streamplatform.streamregistry.handler.Handler;
import com.expediagroup.streamplatform.streamregistry.model.Entity;
import com.expediagroup.streamplatform.streamregistry.model.Specification;

@RequiredArgsConstructor
public class IdentityHandler<T extends Entity> implements Handler<T> {
  public static final String DEFAULT = "default";
  private final String type;
  private final Class<T> target;

  @Override
  public String type() {
    return type;
  }

  @Override
  public Class<T> target() {
    return target;
  }

  @Override
  public Specification handleInsert(T entity) {
    return entity.getSpecification();
  }

  @Override
  public Specification handleUpdate(T entity, T existing) {
    return entity.getSpecification();
  }

  @Override
  public void handleDelete(T entity) {

  }

}
