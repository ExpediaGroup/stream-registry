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

import com.expediagroup.streamplatform.streamregistry.core.augmentors.StreamAugmentor;
import com.expediagroup.streamplatform.streamregistry.core.filters.Filter;
import com.expediagroup.streamplatform.streamregistry.core.repositories.StreamRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.StreamValidator;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;

@Component
public class StreamService {

  StreamRepository streamRepository;
  StreamValidator streamValidator;
  StreamAugmentor streamAugmentor;

  public StreamService(
      StreamRepository streamRepository,
      StreamValidator streamValidator,
      StreamAugmentor streamAugmentor) {
    this.streamRepository = streamRepository;
    this.streamValidator = streamValidator;
    this.streamAugmentor = streamAugmentor;
  }

  public Optional<Stream> create(Stream stream) throws ValidationException {
    if (stream.getKey() != null && streamRepository.findById(stream.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    streamValidator.validateForCreate(stream);
    stream = streamAugmentor.augmentForCreate(stream);
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
    stream = streamAugmentor.augmentForUpdate(stream, existing.get());
    streamValidator.validateForUpdate(stream, existing.get());
    return Optional.ofNullable(streamRepository.save(stream));
  }

  public Optional<Stream> upsert(Stream stream) throws ValidationException {
    return !streamRepository.findById(stream.getKey()).isPresent() ?
        create(stream) :
        update(stream);
  }

  public void delete(Stream stream) {
  }

  public Iterable<Stream> findAll(Filter<Stream> filter) {
    return stream(streamRepository.findAll().spliterator(), false)
        .filter(r -> filter.matches(r))
        .collect(Collectors.toList());
  }
}