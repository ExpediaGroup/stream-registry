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

import com.expediagroup.streamplatform.streamregistry.app.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.app.ValidationException;
import com.expediagroup.streamplatform.streamregistry.app.concrete.augmentors.ConsumerBindingAugmentor;
import com.expediagroup.streamplatform.streamregistry.app.concrete.validators.ConsumerBindingValidator;
import com.expediagroup.streamplatform.streamregistry.app.filters.Filter;
import com.expediagroup.streamplatform.streamregistry.app.keys.ConsumerBindingKey;
import com.expediagroup.streamplatform.streamregistry.app.repositories.ConsumerBindingRepository;

@Component
public class ConsumerBindingService {

  ConsumerBindingRepository consumerbindingRepository;
  ConsumerBindingValidator consumerbindingValidator;
  ConsumerBindingAugmentor consumerbindingAugmentor;


  public ConsumerBindingService(
      ConsumerBindingRepository consumerbindingRepository,
      ConsumerBindingValidator consumerbindingValidator,
      ConsumerBindingAugmentor consumerbindingAugmentor) {
    this.consumerbindingRepository = consumerbindingRepository;
    this.consumerbindingValidator = consumerbindingValidator;
    this.consumerbindingAugmentor = consumerbindingAugmentor;
  }

  public Optional<ConsumerBinding> create(ConsumerBinding consumerbinding) throws ValidationException {
    if (consumerbindingRepository.findById(consumerbinding.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    consumerbindingValidator.validateForCreate(consumerbinding);
    consumerbinding = consumerbindingAugmentor.augmentForCreate(consumerbinding);
    return Optional.ofNullable(consumerbindingRepository.save(consumerbinding));
  }

  public Optional<ConsumerBinding> read(ConsumerBindingKey key) {
    return consumerbindingRepository.findById(key);
  }

  public Iterable<ConsumerBinding> readAll() {
    return consumerbindingRepository.findAll();
  }

  public Optional<ConsumerBinding> update(ConsumerBinding consumerbinding) throws ValidationException {
    Optional<ConsumerBinding> existing = consumerbindingRepository.findById(consumerbinding.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    consumerbinding = consumerbindingAugmentor.augmentForUpdate(consumerbinding, existing.get());
    consumerbindingValidator.validateForUpdate(consumerbinding, existing.get());
    return Optional.ofNullable(consumerbindingRepository.save(consumerbinding));
  }

  public Optional<ConsumerBinding> upsert(ConsumerBinding consumerbinding) throws ValidationException {
    return !consumerbindingRepository.findById(consumerbinding.getKey()).isPresent() ?
        create(consumerbinding) :
        update(consumerbinding);
  }

  public void delete(ConsumerBinding consumerbinding) {
  }

  public Iterable<ConsumerBinding> findAll(Filter<ConsumerBinding> filter){
    return stream(consumerbindingRepository.findAll().spliterator(), false)
        .filter(r -> filter.matches(r))
        .collect(Collectors.toList());
  }

}