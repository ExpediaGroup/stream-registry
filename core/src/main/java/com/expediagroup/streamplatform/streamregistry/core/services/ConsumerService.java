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

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlersForServices;
import com.expediagroup.streamplatform.streamregistry.core.repositories.ConsumerRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.ConsumerValidator;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.DomainKey;

@Component
public class ConsumerService {

  ConsumerRepository consumerRepository;
  ConsumerValidator consumerValidator;
  private HandlersForServices handlerService;

  public ConsumerService(
      ConsumerRepository consumerRepository,
      ConsumerValidator consumerValidator,
      HandlersForServices handlerService) {
    this.consumerRepository = consumerRepository;
    this.consumerValidator = consumerValidator;
    this.handlerService = handlerService;
  }

  public Optional<Consumer> create(Consumer consumer) throws ValidationException {
    if (consumerRepository.findById(consumer.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    consumerValidator.validateForCreate(consumer);
    consumer.setSpecification(handlerService.handleInsert(consumer));
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
    consumerValidator.validateForUpdate(consumer, existing.get());
    consumer.setSpecification(handlerService.handleUpdate(consumer, existing.get()));
    return Optional.ofNullable(consumerRepository.save(consumer));
  }

  public Optional<Consumer> upsert(Consumer consumer) throws ValidationException {
    return !consumerRepository.findById(consumer.getKey()).isPresent() ?
        create(consumer) :
        update(consumer);
  }

  public void delete(Consumer consumer) {
  }

  public Iterable<Consumer> findAll(Predicate<Consumer> filter) {
    return stream(consumerRepository.findAll().spliterator(), false)
        .filter(r -> filter.test(r))
        .collect(Collectors.toList());
  }

  public void validateConsumerExists(ConsumerKey key) {
    if (read(key).isEmpty()) {
      throw new ValidationException("Consumer does not exist");
    }
  }
}