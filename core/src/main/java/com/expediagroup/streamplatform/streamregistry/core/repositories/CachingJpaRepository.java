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
package com.expediagroup.streamplatform.streamregistry.core.repositories;

import static org.hibernate.annotations.QueryHints.CACHEABLE;

import java.util.List;
import java.util.Optional;
import javax.persistence.QueryHint;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CachingJpaRepository<T, ID> extends JpaRepository<T, ID> {
  @QueryHints(@QueryHint(name = CACHEABLE, value = "true"))
  Optional<T> findById(ID id);

  @QueryHints(@QueryHint(name = CACHEABLE, value = "true"))
  boolean existsById(ID id);

  @QueryHints(@QueryHint(name = CACHEABLE, value = "true"))
  List<T> findAll();

  @QueryHints(@QueryHint(name = CACHEABLE, value = "true"))
  List<T> findAllById(Iterable<ID> ids);

  @QueryHints(@QueryHint(name = CACHEABLE, value = "true"))
  <S extends T> List<S> findAll(Example<S> example);
}
