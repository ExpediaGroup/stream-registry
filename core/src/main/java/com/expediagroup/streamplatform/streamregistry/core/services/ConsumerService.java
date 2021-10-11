/**
 * Copyright (C) 2018-2021 Expedia, Inc.
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

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;
import lombok.val;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.ConsumerValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.core.views.ConsumerBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.ConsumerView;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.repository.ConsumerRepository;

@Component
@RequiredArgsConstructor
public class ConsumerService {
  private final ConsumerView consumerView;
  private final HandlerService handlerService;
  private final ConsumerValidator consumerValidator;
  private final ConsumerRepository consumerRepository;
  private final ConsumerBindingService consumerBindingService;
  private final ConsumerBindingView consumerBindingView;

  @PreAuthorize("hasPermission(#consumer, 'CREATE')")
  public Optional<Consumer> create(Consumer consumer) throws ValidationException {
    if (consumerView.get(consumer.getKey()).isPresent()) {
      throw new ValidationException("Can't create " + consumer.getKey() + " because it already exists");
    }
    consumerValidator.validateForCreate(consumer);
    consumer.setSpecification(handlerService.handleInsert(consumer));
    return save(consumer);
  }

  @PreAuthorize("hasPermission(#consumer, 'UPDATE')")
  public Optional<Consumer> update(Consumer consumer) throws ValidationException {
    val existing = consumerView.get(consumer.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update " + consumer.getKey().getName() + " because it doesn't exist");
    }
    consumerValidator.validateForUpdate(consumer, existing.get());
    consumer.setSpecification(handlerService.handleUpdate(consumer, existing.get()));
    return save(consumer);
  }

  @PreAuthorize("hasPermission(#consumer, 'UPDATE_STATUS')")
  public Optional<Consumer> updateStatus(Consumer consumer, Status status) {
    consumer.setStatus(status);
    return save(consumer);
  }

  private Optional<Consumer> save(Consumer consumer) {
    return Optional.ofNullable(consumerRepository.save(consumer));
  }

  @PostAuthorize("returnObject.isPresent() ? hasPermission(returnObject, 'READ') : true")
  public Optional<Consumer> get(ConsumerKey key) {
    return consumerView.get(key);
  }

  @PostFilter("hasPermission(filterObject, 'READ')")
  public List<Consumer> findAll(Predicate<Consumer> filter) {
    return consumerView.findAll(filter).collect(toList());
  }

  @PreAuthorize("hasPermission(#consumer, 'DELETE')")
  public void delete(Consumer consumer) {
    handlerService.handleDelete(consumer);
    consumerBindingView
      .findAll(b -> b.getKey().getConsumerKey().equals(consumer.getKey()))
      .forEach(consumerBindingService::delete);
    consumerRepository.delete(consumer);
  }

  @PreAuthorize("hasPermission(#consumer, 'CREATE')")
  public Consumer canCreateConsumer(Consumer consumer) {
    return consumer;
  }

  @PreAuthorize("hasPermission(#consumer, 'UPDATE')")
  public Consumer canUpdateConsumer(Consumer consumer) {
    return consumer;
  }

  @PreAuthorize("hasPermission(#consumer, 'DELETE')")
  public Consumer canDeleteConsumer(Consumer consumer) {
    return consumer;
  }
}
