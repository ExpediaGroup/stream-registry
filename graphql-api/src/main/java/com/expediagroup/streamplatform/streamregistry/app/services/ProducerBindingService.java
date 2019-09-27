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

import com.expediagroup.streamplatform.streamregistry.app.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.app.ValidationException;
import com.expediagroup.streamplatform.streamregistry.app.concrete.augmentors.ProducerBindingAugmentor;
import com.expediagroup.streamplatform.streamregistry.app.concrete.validators.ProducerBindingValidator;
import com.expediagroup.streamplatform.streamregistry.app.filters.Filter;
import com.expediagroup.streamplatform.streamregistry.app.keys.ProducerBindingKey;
import com.expediagroup.streamplatform.streamregistry.app.repositories.ProducerBindingRepository;

@Component
public class ProducerBindingService {

  ProducerBindingRepository producerbindingRepository;
  ProducerBindingValidator producerbindingValidator;
  ProducerBindingAugmentor producerbindingAugmentor;

  public ProducerBindingService(
      ProducerBindingRepository producerbindingRepository,
      ProducerBindingValidator producerbindingValidator,
      ProducerBindingAugmentor producerbindingAugmentor) {
    this.producerbindingRepository = producerbindingRepository;
    this.producerbindingValidator = producerbindingValidator;
    this.producerbindingAugmentor = producerbindingAugmentor;
  }

  public Optional<ProducerBinding> create(ProducerBinding producerbinding) throws ValidationException {
    if (producerbindingRepository.findById(producerbinding.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    producerbindingValidator.validateForCreate(producerbinding);
    producerbinding = producerbindingAugmentor.augmentForCreate(producerbinding);
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
    producerbinding = producerbindingAugmentor.augmentForUpdate(producerbinding, existing.get());
    producerbindingValidator.validateForUpdate(producerbinding, existing.get());
    return Optional.ofNullable(producerbindingRepository.save(producerbinding));
  }

  public Optional<ProducerBinding> upsert(ProducerBinding producerbinding) throws ValidationException {
    return !producerbindingRepository.findById(producerbinding.getKey()).isPresent() ?
        create(producerbinding) :
        update(producerbinding);
  }

  public void delete(ProducerBinding producerbinding) {
  }

  public Iterable<ProducerBinding> findAll(Filter<ProducerBinding> filter){
    return stream(producerbindingRepository.findAll().spliterator(), false)
        .filter(r -> filter.matches(r))
        .collect(Collectors.toList());
  }
}