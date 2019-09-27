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

import com.expediagroup.streamplatform.streamregistry.app.Producer;
import com.expediagroup.streamplatform.streamregistry.app.ValidationException;
import com.expediagroup.streamplatform.streamregistry.app.concrete.augmentors.ProducerAugmentor;
import com.expediagroup.streamplatform.streamregistry.app.concrete.validators.ProducerValidator;
import com.expediagroup.streamplatform.streamregistry.app.filters.Filter;
import com.expediagroup.streamplatform.streamregistry.app.keys.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.app.repositories.ProducerRepository;

@Component
public class ProducerService {

  ProducerRepository producerRepository;
  ProducerValidator producerValidator;
  ProducerAugmentor producerAugmentor;

  public ProducerService(
      ProducerRepository producerRepository,
      ProducerValidator producerValidator,
      ProducerAugmentor producerAugmentor) {
    this.producerRepository = producerRepository;
    this.producerValidator = producerValidator;
    this.producerAugmentor = producerAugmentor;
  }

  public Optional<Producer> create(Producer producer) throws ValidationException {
    if (producerRepository.findById(producer.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    producerValidator.validateForCreate(producer);
    producer = producerAugmentor.augmentForCreate(producer);
    return Optional.ofNullable(producerRepository.save(producer));
  }

  public Optional<Producer> read(ProducerKey key) {
    return producerRepository.findById(key);
  }

  public Iterable<Producer> readAll() {
    return producerRepository.findAll();
  }

  public Optional<Producer> update(Producer producer) throws ValidationException {
    Optional<Producer> existing = producerRepository.findById(producer.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    producer = producerAugmentor.augmentForUpdate(producer, existing.get());
    producerValidator.validateForUpdate(producer, existing.get());
    return Optional.ofNullable(producerRepository.save(producer));
  }

  public Optional<Producer> upsert(Producer producer) throws ValidationException {
    return !producerRepository.findById(producer.getKey()).isPresent() ?
        create(producer) :
        update(producer);
  }

  public void delete(Producer producer) {
  }

  public Iterable<Producer> findAll(Filter<Producer> filter){
    return stream(producerRepository.findAll().spliterator(), false)
        .filter(r -> filter.matches(r))
        .collect(Collectors.toList());
  }
}