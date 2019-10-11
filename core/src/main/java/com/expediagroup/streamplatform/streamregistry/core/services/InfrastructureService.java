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

import static java.util.stream.StreamSupport.stream;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.handler.HandlersForServices;
import com.expediagroup.streamplatform.streamregistry.core.repositories.InfrastructureRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.InfrastructureValidator;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.model.keys.InfrastructureKey;

@Component
public class InfrastructureService {

  InfrastructureRepository infrastructureRepository;
  InfrastructureValidator infrastructureValidator;
  private HandlersForServices handlerService;

  public InfrastructureService(
      InfrastructureRepository infrastructureRepository,
      InfrastructureValidator infrastructureValidator,
      HandlersForServices handlerService) {

    this.infrastructureRepository = infrastructureRepository;
    this.infrastructureValidator = infrastructureValidator;
    this.handlerService = handlerService;
  }

  public Optional<Infrastructure> create(Infrastructure infrastructure) throws ValidationException {
    if (infrastructureRepository.findById(infrastructure.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    infrastructureValidator.validateForCreate(infrastructure);
    infrastructure.setSpecification(handlerService.handleInsert(infrastructure));
    return Optional.ofNullable(infrastructureRepository.save(infrastructure));
  }

  public Optional<Infrastructure> read(InfrastructureKey key) {
    return infrastructureRepository.findById(key);
  }

  public Iterable<Infrastructure> readAll() {
    return infrastructureRepository.findAll();
  }

  public Optional<Infrastructure> update(Infrastructure infrastructure) throws ValidationException {
    Optional<Infrastructure> existing = infrastructureRepository.findById(infrastructure.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    infrastructureValidator.validateForUpdate(infrastructure, existing.get());
    infrastructure.setSpecification(handlerService.handleUpdate(infrastructure, existing.get()));
    return Optional.ofNullable(infrastructureRepository.save(infrastructure));
  }

  public Optional<Infrastructure> upsert(Infrastructure infrastructure) throws ValidationException {
    return !infrastructureRepository.findById(infrastructure.getKey()).isPresent() ?
        create(infrastructure) :
        update(infrastructure);
  }

  public void delete(Infrastructure infrastructure) {
  }

  public Iterable<Infrastructure> findAll(Filter<Infrastructure> filter) {
    return stream(infrastructureRepository.findAll().spliterator(), false)
        .filter(r -> filter.matches(r))
        .collect(Collectors.toList());
  }
}