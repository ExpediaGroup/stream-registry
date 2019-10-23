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

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlersForServices;
import com.expediagroup.streamplatform.streamregistry.core.repositories.StreamBindingRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.StreamBindingValidator;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamBindingKey;

@Component
@RequiredArgsConstructor
public class StreamBindingService {
  private final HandlersForServices handlerService;
  private final StreamBindingValidator streambindingValidator;
  private final StreamBindingRepository streambindingRepository;

  public Optional<StreamBinding> create(StreamBinding streambinding) throws ValidationException {
    if (streambindingRepository.findById(streambinding.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    streambindingValidator.validateForCreate(streambinding);
    streambinding.setSpecification(handlerService.handleInsert(streambinding));
    return Optional.ofNullable(streambindingRepository.save(streambinding));
  }

  public Optional<StreamBinding> read(StreamBindingKey key) {
    return streambindingRepository.findById(key);
  }

  public Iterable<StreamBinding> readAll() {
    return streambindingRepository.findAll();
  }

  public Optional<StreamBinding> update(StreamBinding streambinding) throws ValidationException {
    Optional<StreamBinding> existing = streambindingRepository.findById(streambinding.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    streambindingValidator.validateForUpdate(streambinding, existing.get());
    streambinding.setSpecification(handlerService.handleUpdate(streambinding, existing.get()));
    return Optional.ofNullable(streambindingRepository.save(streambinding));
  }

  public Optional<StreamBinding> upsert(StreamBinding streambinding) throws ValidationException {
    return !streambindingRepository.findById(streambinding.getKey()).isPresent() ?
        create(streambinding) :
        update(streambinding);
  }

  public void delete(StreamBinding streambinding) {
  }

  public Iterable<StreamBinding> findAll(Predicate<StreamBinding> filter) {
    return stream(streambindingRepository.findAll().spliterator(), false)
        .filter(r -> filter.test(r))
        .collect(Collectors.toList());
  }

  public void validateStreamBindingExists(StreamBindingKey key) {
    if (read(key).isEmpty()) {
      throw new ValidationException("StreamBinding does not exist");
    }
  }
}