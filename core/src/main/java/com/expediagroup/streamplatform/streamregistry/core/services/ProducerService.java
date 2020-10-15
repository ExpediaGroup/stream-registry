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

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.ProducerValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.repository.ProducerRepository;

@Component
@RequiredArgsConstructor
public class ProducerService {
  private final HandlerService handlerService;
  private final ProducerValidator producerValidator;
  private final ProducerRepository producerRepository;

  @PreAuthorize("hasPermission(#producer, 'create')")
  public Optional<Producer> create(Producer producer) throws ValidationException {
    if (read(producer.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    producerValidator.validateForCreate(producer);
    producer.setSpecification(handlerService.handleInsert(producer));
    return save(producer);
  }

  @PreAuthorize("hasPermission(#producer, 'update')")
  public Optional<Producer> update(Producer producer) throws ValidationException {
    var existing = read(producer.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update " + producer.getKey().getName() + " because it doesn't exist");
    }
    producerValidator.validateForUpdate(producer, existing.get());
    producer.setSpecification(handlerService.handleUpdate(producer, existing.get()));
    return save(producer);
  }

  @PreAuthorize("hasPermission(#producerKey, 'updateStatus')")
  public Optional<Producer> updateStatus(ProducerKey producerKey, Status status) {
    Producer producer = read(producerKey).get();
    producer.setStatus(status);
    return save(producer);
  }

  private Optional<Producer> save(Producer producer) {
    producer = producerRepository.save(producer);
    return Optional.ofNullable(producer);
  }

  public Optional<Producer> upsert(Producer producer) throws ValidationException {
    return !read(producer.getKey()).isPresent() ? create(producer) : update(producer);
  }

  public Optional<Producer> read(ProducerKey key) {
    return producerRepository.findById(key);
  }

  public List<Producer> findAll(Predicate<Producer> filter) {
    return producerRepository.findAll().stream().filter(filter).collect(toList());
  }

  @PreAuthorize("hasPermission(#producer, 'delete')")
  public void delete(Producer producer) {
    throw new UnsupportedOperationException();
  }

  public boolean exists(ProducerKey key) {
    return read(key).isPresent();
  }
}
