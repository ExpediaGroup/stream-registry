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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlersForServices;
import com.expediagroup.streamplatform.streamregistry.core.repositories.ProducerBindingRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.ProducerBindingValidator;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerBindingKey;

@Component
@RequiredArgsConstructor
public class ProducerBindingService {
  private final HandlersForServices handlerService;
  private final ProducerBindingValidator producerbindingValidator;
  private final ProducerBindingRepository producerbindingRepository;

  public Optional<ProducerBinding> create(ProducerBinding producerbinding) throws ValidationException {
    if (producerbindingRepository.findById(producerbinding.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    producerbindingValidator.validateForCreate(producerbinding);
    producerbinding.setSpecification(handlerService.handleInsert(producerbinding));
    return Optional.ofNullable(producerbindingRepository.save(producerbinding));
  }

  public Optional<ProducerBinding> read(ProducerBindingKey key) {
    return producerbindingRepository.findById(key);
  }

  public Iterable<ProducerBinding> readAll() {
    return producerbindingRepository.findAll();
  }

  public Optional<ProducerBinding> update(ProducerBinding producerbinding) throws ValidationException {
    Optional<ProducerBinding> existing = producerbindingRepository.findById(producerbinding.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    producerbindingValidator.validateForUpdate(producerbinding, existing.get());
    producerbinding.setSpecification(handlerService.handleUpdate(producerbinding, existing.get()));
    return Optional.ofNullable(producerbindingRepository.save(producerbinding));
  }

  public Optional<ProducerBinding> upsert(ProducerBinding producerbinding) throws ValidationException {
    return !producerbindingRepository.findById(producerbinding.getKey()).isPresent() ?
        create(producerbinding) :
        update(producerbinding);
  }

  public void delete(ProducerBinding producerbinding) {
  }

  public Iterable<ProducerBinding> findAll(Predicate<ProducerBinding> filter) {
    return stream(producerbindingRepository.findAll().spliterator(), false)
        .filter(r -> filter.test(r))
        .collect(Collectors.toList());
  }

  public void validateProducerBindingExists(ProducerBindingKey key) {
    if (read(key).isEmpty()) {
      throw new ValidationException("ProducerBinding does not exist");
    }
  }
}