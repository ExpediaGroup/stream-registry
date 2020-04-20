/**
 * Copyright (C) 2018-2020 Expedia, Inc.
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

import static java.util.stream.Collectors.toList;

import java.util.Optional;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.events.EventType;
import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventEmitter;
import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.repositories.ConsumerBindingRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.ConsumerBindingValidator;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;

@Component
@RequiredArgsConstructor
public class ConsumerBindingService {
  private final HandlerService handlerService;
  private final ConsumerBindingValidator consumerBindingValidator;
  private final ConsumerBindingRepository consumerBindingRepository;
  private final NotificationEventEmitter<ConsumerBinding> consumerBindingServiceEventEmitter;

  public Optional<ConsumerBinding> create(ConsumerBinding consumerBinding) throws ValidationException {
    if (consumerBindingRepository.findById(consumerBinding.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    consumerBindingValidator.validateForCreate(consumerBinding);
    consumerBinding.setSpecification(handlerService.handleInsert(consumerBinding));
    return consumerBindingServiceEventEmitter.emitEventOnProcessedEntity(EventType.CREATE, consumerBindingRepository.save(consumerBinding));
  }

  public Optional<ConsumerBinding> read(ConsumerBindingKey key) {
    return consumerBindingRepository.findById(key);
  }

  public Iterable<ConsumerBinding> readAll() {
    return consumerBindingRepository.findAll();
  }

  public Optional<ConsumerBinding> update(ConsumerBinding consumerBinding) throws ValidationException {
    Optional<ConsumerBinding> existing = consumerBindingRepository.findById(consumerBinding.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    consumerBindingValidator.validateForUpdate(consumerBinding, existing.get());
    consumerBinding.setSpecification(handlerService.handleInsert(consumerBinding));
    return consumerBindingServiceEventEmitter.emitEventOnProcessedEntity(EventType.UPDATE, consumerBindingRepository.save(consumerBinding));
  }

  public Optional<ConsumerBinding> upsert(ConsumerBinding consumerBinding) throws ValidationException {
    return !consumerBindingRepository.findById(consumerBinding.getKey()).isPresent() ?
        create(consumerBinding) :
        update(consumerBinding);
  }

  public void delete(ConsumerBinding consumerBinding) {
    throw new UnsupportedOperationException();
  }

  public Iterable<ConsumerBinding> findAll(Predicate<ConsumerBinding> filter) {
    return consumerBindingRepository.findAll().stream().filter(filter).collect(toList());
  }

  public Optional<ConsumerBinding> find(ConsumerKey key) {
    ConsumerBinding example = new ConsumerBinding(
        new ConsumerBindingKey(
            key.getStreamDomain(),
            key.getStreamName(),
            key.getStreamVersion(),
            key.getZone(),
            null,
            key.getName()
        ), null, null);
    return consumerBindingRepository.findAll(Example.of(example)).stream().findFirst();
  }

  public void validateConsumerBindingExists(ConsumerBindingKey key) {
    if (read(key).isEmpty()) {
      throw new ValidationException("ConsumerBinding does not exist");
    }
  }
}
