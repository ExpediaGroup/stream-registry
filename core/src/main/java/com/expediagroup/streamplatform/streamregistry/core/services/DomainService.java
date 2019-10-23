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
package com.expediagroup.streamplatform.streamregistry.core.services;

/**
 * Copyright (C) 2016-2019 Expedia Inc.
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

import static java.util.stream.StreamSupport.stream;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlersForServices;
import com.expediagroup.streamplatform.streamregistry.core.repositories.DomainRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.DomainValidator;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.keys.DomainKey;

@Component
@RequiredArgsConstructor
public class DomainService {
  private final HandlersForServices handlerService;
  private final DomainValidator domainValidator;
  private final DomainRepository domainRepository;

  public Optional<Domain> create(Domain domain) throws ValidationException {
    if (domainRepository.findById(domain.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    domainValidator.validateForCreate(domain);
    domain.setSpecification(handlerService.handleInsert(domain));
    return Optional.ofNullable(domainRepository.save(domain));
  }

  public Optional<Domain> read(DomainKey key) {
    return domainRepository.findById(key);
  }

  public Iterable<Domain> readAll() {
    return domainRepository.findAll();
  }

  public Optional<Domain> update(Domain domain) throws ValidationException {
    Optional<Domain> existing = domainRepository.findById(domain.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update " + domain.getKey() + " because it doesn't exist");
    }
    domainValidator.validateForUpdate(domain, existing.get());
    domain.setSpecification(handlerService.handleUpdate(domain, existing.get()));
    return Optional.ofNullable(domainRepository.save(domain));
  }

  public Optional<Domain> upsert(Domain domain) throws ValidationException {
    return !domainRepository.findById(domain.getKey()).isPresent() ?
        create(domain) :
        update(domain);
  }

  public void delete(Domain domain) {
  }

  public Iterable<Domain> findAll(Predicate<Domain> filter) {
    return stream(domainRepository.findAll().spliterator(), false)
        .filter(r -> filter.test(r))
        .collect(Collectors.toList());
  }

  public void validateDomainExists(DomainKey key) {
    if (read(key).isEmpty()) {
      throw new ValidationException("Domain does not exist");
    }
  }
}