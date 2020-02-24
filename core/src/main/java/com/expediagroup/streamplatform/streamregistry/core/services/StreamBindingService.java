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

import static java.util.stream.StreamSupport.stream;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.events.EventType;
import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventEmitter;
import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.repositories.StreamBindingRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.StreamBindingValidator;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamBindingKey;

@Component
@RequiredArgsConstructor
public class StreamBindingService {
  private final HandlerService handlerService;
  private final StreamBindingValidator streamBindingValidator;
  private final StreamBindingRepository streamBindingRepository;
  private final NotificationEventEmitter<StreamBinding> streamBindingServiceEventEmitter;

  public Optional<StreamBinding> create(StreamBinding streamBinding) throws ValidationException {
    if (streamBindingRepository.findById(streamBinding.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    streamBindingValidator.validateForCreate(streamBinding);
    streamBinding.setSpecification(handlerService.handleInsert(streamBinding));
    return streamBindingServiceEventEmitter.emitEventOnProcessedEntity(EventType.CREATE, streamBindingRepository.save(streamBinding));
  }

  public Optional<StreamBinding> read(StreamBindingKey key) {
    return streamBindingRepository.findById(key);
  }

  public Iterable<StreamBinding> readAll() {
    return streamBindingRepository.findAll();
  }

  public Optional<StreamBinding> update(StreamBinding streamBinding) throws ValidationException {
    Optional<StreamBinding> existing = streamBindingRepository.findById(streamBinding.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    streamBindingValidator.validateForUpdate(streamBinding, existing.get());
    streamBinding.setSpecification(handlerService.handleUpdate(streamBinding, existing.get()));
    return streamBindingServiceEventEmitter.emitEventOnProcessedEntity(EventType.UPDATE, streamBindingRepository.save(streamBinding));
  }

  public Optional<StreamBinding> upsert(StreamBinding streamBinding) throws ValidationException {
    return !streamBindingRepository.findById(streamBinding.getKey()).isPresent() ?
        create(streamBinding) :
        update(streamBinding);
  }

  public void delete(StreamBinding streamBinding) {
    throw new UnsupportedOperationException();
  }

  public Iterable<StreamBinding> findAll(Predicate<StreamBinding> filter) {
    return stream(streamBindingRepository.findAll().spliterator(), false)
        .filter(r -> filter.test(r))
        .collect(Collectors.toList());
  }

  public void validateStreamBindingExists(StreamBindingKey key) {
    if (read(key).isEmpty()) {
      throw new ValidationException("StreamBinding does not exist");
    }
  }
}