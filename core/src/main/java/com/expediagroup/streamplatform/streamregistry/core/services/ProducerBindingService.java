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
import com.expediagroup.streamplatform.streamregistry.core.validators.ProducerBindingValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.repository.ProducerBindingRepository;

@Component
@RequiredArgsConstructor
public class ProducerBindingService {
  private final HandlerService handlerService;
  private final ProducerBindingValidator producerBindingValidator;
  private final ProducerBindingRepository producerBindingRepository;

  @PreAuthorize("hasPermission(#producerBinding, 'CREATE')")
  public Optional<ProducerBinding> create(ProducerBinding producerBinding) throws ValidationException {
    if (unsecuredGet(producerBinding.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    producerBindingValidator.validateForCreate(producerBinding);
    producerBinding.setSpecification(handlerService.handleInsert(producerBinding));
    return save(producerBinding);
  }

  @PreAuthorize("hasPermission(#producerBinding, 'UPDATE')")
  public Optional<ProducerBinding> update(ProducerBinding producerBinding) throws ValidationException {
    val existing = unsecuredGet(producerBinding.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update " + producerBinding.getKey() + " because it doesn't exist");
    }
    producerBindingValidator.validateForUpdate(producerBinding, existing.get());
    producerBinding.setSpecification(handlerService.handleUpdate(producerBinding, existing.get()));
    return save(producerBinding);
  }

  @PreAuthorize("hasPermission(#producerBinding, 'UPDATE_STATUS')")
  public Optional<ProducerBinding> updateStatus(ProducerBinding producerBinding, Status status) {
    producerBinding.setStatus(status);
    return save(producerBinding);
  }

  private Optional<ProducerBinding> save(ProducerBinding producerBinding) {
    producerBinding = producerBindingRepository.save(producerBinding);
    return Optional.ofNullable(producerBinding);
  }

  @PostAuthorize("returnObject.isPresent() ? hasPermission(returnObject, 'READ') : true")
  public Optional<ProducerBinding> get(ProducerBindingKey key) {
    return unsecuredGet(key);
  }

  public Optional<ProducerBinding> unsecuredGet(ProducerBindingKey key) {
    return producerBindingRepository.findById(key);
  }

  @PostFilter("hasPermission(filterObject, 'READ')")
  public List<ProducerBinding> findAll(Predicate<ProducerBinding> filter) {
    return producerBindingRepository.findAll().stream().filter(filter).collect(toList());
  }

  @PreAuthorize("hasPermission(#producerBinding, 'DELETE')")
  public void delete(ProducerBinding producerBinding) {
    throw new UnsupportedOperationException();
  }

  public boolean exists(ProducerBindingKey key) {
    return unsecuredGet(key).isPresent();
  }

  @PostAuthorize("returnObject.isPresent() ? hasPermission(returnObject, 'READ') : true")
  public Optional<ProducerBinding> find(ProducerKey key) {
    val example = new ProducerBinding(new ProducerBindingKey(
        key.getStreamDomain(),
        key.getStreamName(),
        key.getStreamVersion(),
        key.getZone(),
        null,
        key.getName()
    ), null, null);
    return producerBindingRepository.findAll(example).stream().findFirst();
  }
}
