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

import com.expediagroup.streamplatform.streamregistry.core.services.unsecured.UnsecuredConsumerService;
import com.expediagroup.streamplatform.streamregistry.core.services.unsecured.UnsecuredProducerService;
import com.expediagroup.streamplatform.streamregistry.core.services.unsecured.UnsecuredSchemaService;
import com.expediagroup.streamplatform.streamregistry.core.services.unsecured.UnsecuredStreamBindingService;
import com.expediagroup.streamplatform.streamregistry.core.services.unsecured.UnsecuredStreamService;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;
import lombok.RequiredArgsConstructor;
import lombok.val;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.StreamValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;
import com.expediagroup.streamplatform.streamregistry.repository.StreamRepository;

@Component
@RequiredArgsConstructor
public class StreamService {
  private final HandlerService handlerService;
  private final StreamValidator streamValidator;
  private final StreamRepository streamRepository;
  private final StreamBindingService streamBindingService;
  private final ProducerService producerService;
  private final ConsumerService consumerService;
  private final SchemaService schemaService;
  private final UnsecuredStreamService unsecuredStreamService;
  private final UnsecuredStreamBindingService unsecuredStreamBindingService;
  private final UnsecuredProducerService unsecuredProducerService;
  private final UnsecuredConsumerService unsecuredConsumerService;
  private final UnsecuredSchemaService unsecuredSchemaService;

  @PreAuthorize("hasPermission(#stream, 'CREATE')")
  public Optional<Stream> create(Stream stream) throws ValidationException {
    if (unsecuredStreamService.get(stream.getKey()).isPresent()) {
      throw new ValidationException("Can't create " + stream.getKey() + " because it already exists");
    }
    streamValidator.validateForCreate(stream);
    stream.setSpecification(handlerService.handleInsert(stream));
    return save(stream);
  }

  @PreAuthorize("hasPermission(#stream, 'UPDATE')")
  public Optional<Stream> update(Stream stream) throws ValidationException {
    val existing = unsecuredStreamService.get(stream.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update " + stream.getKey() + " because it doesn't exist");
    }
    stream.setSchemaKey(existing.get().getSchemaKey());
    streamValidator.validateForUpdate(stream, existing.get());
    stream.setSpecification(handlerService.handleUpdate(stream, existing.get()));
    return save(stream);
  }

  @PreAuthorize("hasPermission(#stream, 'UPDATE_STATUS')")
  public Optional<Stream> updateStatus(Stream stream, Status status) {
    stream.setStatus(status);
    return save(stream);
  }

  private Optional<Stream> save(Stream stream) {
    stream = streamRepository.save(stream);
    return Optional.ofNullable(stream);
  }

  @PostAuthorize("returnObject.isPresent() ? hasPermission(returnObject, 'READ') : true")
  public Optional<Stream> get(StreamKey key) {
    return unsecuredStreamService.get(key);
  }

  @PostFilter("hasPermission(filterObject, 'READ')")
  public List<Stream> findAll(Predicate<Stream> filter) {
    return unsecuredStreamService.findAll(filter).collect(toList());
  }

  @PreAuthorize("hasPermission(#stream, 'DELETE')")
  public void delete(Stream stream) {
    handlerService.handleDelete(stream);

    // This will cascade to ConsumerBinding and ProducerBinding also
    unsecuredStreamBindingService
      .findAll(b -> b.getKey().getStreamKey().equals(stream.getKey()))
      .forEach(streamBindingService::delete);

    unsecuredConsumerService
      .findAll(c -> c.getKey().getStreamKey().equals(stream.getKey()))
      .forEach(consumerService::delete);

    unsecuredProducerService
      .findAll(p -> p.getKey().getStreamKey().equals(stream.getKey()))
      .forEach(producerService::delete);

    streamRepository.delete(stream);

    SchemaKey schemaKey = stream.getSchemaKey();
    boolean schemaReferencedByOtherStreams = unsecuredStreamService.findAll(s -> s.getSchemaKey().equals(schemaKey))
      .filter(s -> s.equals(stream))
      .findAny()
      .isPresent();

    if (!schemaReferencedByOtherStreams) {
      unsecuredSchemaService.get(schemaKey).ifPresent(schemaService::delete);
    }
  }
}
