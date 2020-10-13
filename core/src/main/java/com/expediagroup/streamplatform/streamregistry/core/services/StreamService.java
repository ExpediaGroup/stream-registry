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

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.accesscontrol.AccessControlledService;
import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.StreamValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;
import com.expediagroup.streamplatform.streamregistry.repository.StreamRepository;

@Component
@RequiredArgsConstructor
public class StreamService implements AccessControlledService<StreamKey, Stream>  {
  private final HandlerService handlerService;
  private final StreamValidator streamValidator;
  private final StreamRepository streamRepository;

  @Override
  public Optional<Stream> create(Stream stream) throws ValidationException {
    if (read(stream.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    streamValidator.validateForCreate(stream);
    stream.setSpecification(handlerService.handleInsert(stream));
    return save(stream);
  }

  @Override
  public Optional<Stream> update(Stream stream) throws ValidationException {
    var existing = read(stream.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update " + stream.getKey() + " because it doesn't exist");
    }
    stream.setSchemaKey(existing.get().getSchemaKey());
    streamValidator.validateForUpdate(stream, existing.get());
    stream.setSpecification(handlerService.handleUpdate(stream, existing.get()));
    return save(stream);
  }

  private Optional<Stream> save(Stream stream) {
    stream = streamRepository.save(stream);
    return Optional.ofNullable(stream);
  }

  public Optional<Stream> upsert(Stream stream) throws ValidationException {
    return !read(stream.getKey()).isPresent() ? create(stream) : update(stream);
  }

  public Optional<Stream> read(StreamKey key) {
    return streamRepository.findById(key);
  }

  public List<Stream> findAll(Predicate<Stream> filter) {
    return streamRepository.findAll().stream().filter(filter).collect(toList());
  }

  @Override
  public void delete(Stream stream) {
    throw new UnsupportedOperationException();
  }

  public boolean exists(StreamKey key) {
    return read(key).isPresent();
  }
}
