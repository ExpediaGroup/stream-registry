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

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.ConsumerBindingValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.repository.ConsumerBindingRepository;

@Component
@RequiredArgsConstructor
public class ConsumerBindingService {

  private final HandlerService handlerService;
  private final ConsumerBindingValidator consumerBindingValidator;
  private final ConsumerBindingRepository consumerBindingRepository;

  @PreAuthorize("hasPermission(#consumerBinding, 'CREATE')")
  public Optional<ConsumerBinding> create(ConsumerBinding consumerBinding) throws ValidationException {
    if (read(consumerBinding.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    consumerBindingValidator.validateForCreate(consumerBinding);
    consumerBinding.setSpecification(handlerService.handleInsert(consumerBinding));
    return save(consumerBinding);
  }

  @PreAuthorize("hasPermission(#consumerBinding, 'UPDATE')")
  public Optional<ConsumerBinding> update(ConsumerBinding consumerBinding) throws ValidationException {
    var existing = read(consumerBinding.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update " + consumerBinding.getKey() + " because it doesn't exist");
    }
    consumerBindingValidator.validateForUpdate(consumerBinding, existing.get());
    consumerBinding.setSpecification(handlerService.handleUpdate(consumerBinding, existing.get()));
    return save(consumerBinding);
  }

  @PreAuthorize("hasPermission(#consumerBinding, 'UPDATE_STATUS')")
  public Optional<ConsumerBinding> updateStatus(ConsumerBinding consumerBinding, Status status) {
    consumerBinding.setStatus(status);
    return save(consumerBinding);
  }

  private Optional<ConsumerBinding> save(ConsumerBinding consumerBinding) {
    consumerBinding = consumerBindingRepository.save(consumerBinding);
    return Optional.ofNullable(consumerBinding);
  }

  @PostAuthorize("returnObject.isEmpty() ? true: hasPermission(returnObject, 'READ')")
  public Optional<ConsumerBinding> get(ConsumerBindingKey key) {
    return read(key);
  }

  public Optional<ConsumerBinding> read(ConsumerBindingKey key) {
    return consumerBindingRepository.findById(key);
  }

  @PostFilter("hasPermission(filterObject, 'READ')")
  public List<ConsumerBinding> findAll(Predicate<ConsumerBinding> filter) {
    return consumerBindingRepository.findAll().stream().filter(filter).collect(toList());
  }

  @PreAuthorize("hasPermission(#consumerBinding, 'DELETE')")
  public void delete(ConsumerBinding consumerBinding) {
    throw new UnsupportedOperationException();
  }

  public boolean exists(ConsumerBindingKey key) {
    return read(key).isPresent();
  }

  @PostAuthorize("returnObject.isEmpty() ? true: hasPermission(returnObject, 'READ')")
  public Optional<ConsumerBinding> find(ConsumerKey key) {
    var example = new ConsumerBinding(new ConsumerBindingKey(
        key.getStreamDomain(),
        key.getStreamName(),
        key.getStreamVersion(),
        key.getZone(),
        null,
        key.getName()
    ), null, null);
    return consumerBindingRepository.findAll(example).stream().findFirst();
  }
}
