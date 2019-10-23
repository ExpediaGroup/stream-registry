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
import com.expediagroup.streamplatform.streamregistry.core.repositories.StreamRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.StreamValidator;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;

@Component
public class StreamService {

  StreamRepository streamRepository;
  StreamValidator streamValidator;
  private HandlersForServices handlerService;

  public StreamService(
      StreamRepository streamRepository,
      StreamValidator streamValidator,
      HandlersForServices handlerService) {
    this.streamRepository = streamRepository;
    this.streamValidator = streamValidator;
    this.handlerService = handlerService;
  }

  public Optional<Stream> create(Stream stream) throws ValidationException {
    if (stream.getKey() != null && streamRepository.findById(stream.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    streamValidator.validateForCreate(stream);
    stream.setSpecification(handlerService.handleInsert(stream));
    return Optional.ofNullable(streamRepository.save(stream));
  }

  public Optional<Stream> read(StreamKey key) {
    return streamRepository.findById(key);
  }

  public Iterable<Stream> readAll() {
    return streamRepository.findAll();
  }

  public Optional<Stream> update(Stream stream) throws ValidationException {
    Optional<Stream> existing = streamRepository.findById(stream.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    streamValidator.validateForUpdate(stream, existing.get());
    stream.setSpecification(handlerService.handleUpdate(stream, existing.get()));
    return Optional.ofNullable(streamRepository.save(stream));
  }

  public Optional<Stream> upsert(Stream stream) throws ValidationException {
    return !streamRepository.findById(stream.getKey()).isPresent() ?
        create(stream) :
        update(stream);
  }

  public void delete(Stream stream) {
  }

  public Iterable<Stream> findAll(Predicate<Stream> filter) {
    return stream(streamRepository.findAll().spliterator(), false)
        .filter(r -> filter.test(r))
        .collect(Collectors.toList());
  }

  public void validateStreamExists(StreamKey key) {
    if (read(key).isEmpty()) {
      throw new ValidationException("Stream does not exist");
    }
  }
}