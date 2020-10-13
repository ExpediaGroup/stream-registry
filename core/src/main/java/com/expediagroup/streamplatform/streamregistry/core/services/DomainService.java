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
package com.expediagroup.streamplatform.streamregistry.core.services;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.accesscontrol.AccessControlledService;
import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.DomainValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.keys.DomainKey;
import com.expediagroup.streamplatform.streamregistry.repository.DomainRepository;

@Component
@RequiredArgsConstructor
public class DomainService implements AccessControlledService<DomainKey, Domain> {
  private final HandlerService handlerService;
  private final DomainValidator domainValidator;
  private final DomainRepository domainRepository;

  @Override
  public Optional<Domain> create(Domain entity) throws ValidationException {
    if (read(entity.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    domainValidator.validateForCreate(entity);
    entity.setSpecification(handlerService.handleInsert(entity));
    return save(entity);
  }

  @Override
  public Optional<Domain> update(Domain entity) throws ValidationException {
    var existing = read(entity.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update " + entity.getKey().getName() + " because it doesn't exist");
    }
    domainValidator.validateForUpdate(entity, existing.get());
    entity.setSpecification(handlerService.handleUpdate(entity, existing.get()));
    return save(entity);
  }

  private Optional<Domain> save(Domain domain) {
    domain = domainRepository.save(domain);
    return Optional.ofNullable(domain);
  }

  public Optional<Domain> upsert(Domain domain) throws ValidationException {
    return !read(domain.getKey()).isPresent() ? create(domain) : update(domain);
  }

  public Optional<Domain> read(DomainKey key) {
    return domainRepository.findById(key);
  }

  public List<Domain> findAll(Predicate<Domain> filter) {
    return domainRepository.findAll().stream().filter(filter).collect(toList());
  }

  @Override
  public void delete(Domain entity) {
    throw new UnsupportedOperationException();
  }

  public boolean exists(DomainKey key) {
    return read(key).isPresent();
  }
}
