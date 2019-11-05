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

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.repositories.ProducerBindingRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.ProducerBindingValidator;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerBindingKey;

@Component
@RequiredArgsConstructor
public class ProducerBindingService {
  private final HandlerService handlerService;
  private final ProducerBindingValidator producerBindingValidator;
  private final ProducerBindingRepository producerBindingRepository;

  public Optional<ProducerBinding> create(ProducerBinding producerBinding) throws ValidationException {
    if (producerBindingRepository.findById(producerBinding.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    producerBindingValidator.validateForCreate(producerBinding);
    producerBinding.setSpecification(handlerService.handleInsert(producerBinding));
    return Optional.ofNullable(producerBindingRepository.save(producerBinding));
  }

  public Optional<ProducerBinding> read(ProducerBindingKey key) {
    return producerBindingRepository.findById(key);
  }

  public Iterable<ProducerBinding> readAll() {
    return producerBindingRepository.findAll();
  }

  public Optional<ProducerBinding> update(ProducerBinding producerBinding) throws ValidationException {
    Optional<ProducerBinding> existing = producerBindingRepository.findById(producerBinding.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    producerBindingValidator.validateForUpdate(producerBinding, existing.get());
    producerBinding.setSpecification(handlerService.handleUpdate(producerBinding, existing.get()));
    return Optional.ofNullable(producerBindingRepository.save(producerBinding));
  }

  public Optional<ProducerBinding> upsert(ProducerBinding producerBinding) throws ValidationException {
    return !producerBindingRepository.findById(producerBinding.getKey()).isPresent() ?
        create(producerBinding) :
        update(producerBinding);
  }

  public void delete(ProducerBinding producerBinding) {
    throw new UnsupportedOperationException();
  }

  public Iterable<ProducerBinding> findAll(Predicate<ProducerBinding> filter) {
    return stream(producerBindingRepository.findAll().spliterator(), false)
        .filter(r -> filter.test(r))
        .collect(Collectors.toList());
  }

  public void validateProducerBindingExists(ProducerBindingKey key) {
    if (read(key).isEmpty()) {
      throw new ValidationException("ProducerBinding does not exist");
    }
  }
}