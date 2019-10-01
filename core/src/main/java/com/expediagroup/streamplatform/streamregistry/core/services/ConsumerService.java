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
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.augmentors.ConsumerAugmentor;
import com.expediagroup.streamplatform.streamregistry.core.repositories.ConsumerRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.ConsumerValidator;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;

@Component
public class ConsumerService {

  ConsumerRepository consumerRepository;
  ConsumerValidator consumerValidator;
  ConsumerAugmentor consumerAugmentor;

  public ConsumerService(
      ConsumerRepository consumerRepository,
      ConsumerValidator consumerValidator,
      ConsumerAugmentor consumerAugmentor) {
    this.consumerRepository = consumerRepository;
    this.consumerValidator = consumerValidator;
    this.consumerAugmentor = consumerAugmentor;
  }

  public Optional<Consumer> create(Consumer consumer) throws ValidationException {
    if (consumerRepository.findById(consumer.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    consumerValidator.validateForCreate(consumer);
    consumer = consumerAugmentor.augmentForCreate(consumer);
    return Optional.ofNullable(consumerRepository.save(consumer));
  }

  public Optional<Consumer> read(ConsumerKey key) {
    return consumerRepository.findById(key);
  }

  public Iterable<Consumer> readAll() {
    return consumerRepository.findAll();
  }

  public Optional<Consumer> update(Consumer consumer) throws ValidationException {
    Optional<Consumer> existing = consumerRepository.findById(consumer.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    consumer = consumerAugmentor.augmentForUpdate(consumer, existing.get());
    consumerValidator.validateForUpdate(consumer, existing.get());
    return Optional.ofNullable(consumerRepository.save(consumer));
  }

  public Optional<Consumer> upsert(Consumer consumer) throws ValidationException {
    return !consumerRepository.findById(consumer.getKey()).isPresent() ?
        create(consumer) :
        update(consumer);
  }

  public void delete(Consumer consumer) {
  }

  public Iterable<Consumer> findAll(Filter<Consumer> filter) {
    return stream(consumerRepository.findAll().spliterator(), false)
        .filter(r -> filter.matches(r))
        .collect(Collectors.toList());
  }
}