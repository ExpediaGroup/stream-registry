/**
 * Copyright (C) 2018-2024 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.repository;


import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
  /**
   * @deprecated Use {link {@link #saveSpecification(Object)}} or {link {@link #saveStatus(Object)}}
   * to save the entity specification or entity status independently.
   */
  T save(T entity);

  T saveSpecification(T entity);

  T saveStatus(T entity);

  Optional<T> findById(ID id);

  List<T> findAll();

  /**
   * @deprecated Use {link {@link #findAll()}} and filter the results with predicates.
   */
  @Deprecated
  List<T> findAll(T example);

  void delete(T entity);
}
