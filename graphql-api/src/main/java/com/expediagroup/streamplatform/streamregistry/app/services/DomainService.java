package com.expediagroup.streamplatform.streamregistry.app.services;

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
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.app.Domain;
import com.expediagroup.streamplatform.streamregistry.app.ValidationException;
import com.expediagroup.streamplatform.streamregistry.app.concrete.augmentors.DomainAugmentor;
import com.expediagroup.streamplatform.streamregistry.app.concrete.validators.DomainValidator;
import com.expediagroup.streamplatform.streamregistry.app.filters.Filter;
import com.expediagroup.streamplatform.streamregistry.app.keys.DomainKey;
import com.expediagroup.streamplatform.streamregistry.app.repositories.DomainRepository;

@Component
public class DomainService {

  DomainRepository domainRepository;
  DomainValidator domainValidator;
  DomainAugmentor domainAugmentor;

  public DomainService(
      DomainRepository domainRepository,
      DomainValidator domainValidator,
      DomainAugmentor domainAugmentor) {
    this.domainRepository = domainRepository;
    this.domainValidator = domainValidator;
    this.domainAugmentor = domainAugmentor;
  }

  public Optional<Domain> create(Domain domain) throws ValidationException {
    if (domainRepository.findById(domain.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    domainValidator.validateForCreate(domain);
    domain = domainAugmentor.augmentForCreate(domain);
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
      throw new ValidationException("Can't update because it doesn't exist");
    }
    domain = domainAugmentor.augmentForUpdate(domain, existing.get());
    domainValidator.validateForUpdate(domain, existing.get());
    return Optional.ofNullable(domainRepository.save(domain));
  }

  public Optional<Domain> upsert(Domain domain) throws ValidationException {
    return !domainRepository.findById(domain.getKey()).isPresent() ?
        create(domain) :
        update(domain);
  }

  public void delete(Domain domain) {
  }

  public Iterable<Domain> findAll(Filter<Domain> filter){
    return stream(domainRepository.findAll().spliterator(), false)
        .filter(r -> filter.matches(r))
        .collect(Collectors.toList());
  }

}