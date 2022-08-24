/**
 * Copyright (C) 2018-2022 Expedia, Inc.
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
import java.util.Set;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;
import lombok.val;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.StreamValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.core.views.ConsumerView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProcessView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProducerView;
import com.expediagroup.streamplatform.streamregistry.core.views.SchemaView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamView;
import com.expediagroup.streamplatform.streamregistry.model.Process;
import com.expediagroup.streamplatform.streamregistry.model.ProcessInputStream;
import com.expediagroup.streamplatform.streamregistry.model.ProcessOutputStream;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;
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
  private final ProcessService processService;
  private final StreamView streamView;
  private final StreamBindingView streamBindingView;
  private final ProducerView producerView;
  private final ConsumerView consumerView;
  private final SchemaView schemaView;
  private final ProcessView processView;

  @PreAuthorize("hasPermission(#stream, 'CREATE')")
  public Optional<Stream> create(Stream stream) throws ValidationException {
    if (streamView.get(stream.getKey()).isPresent()) {
      throw new ValidationException("Can't create " + stream.getKey() + " because it already exists");
    }
    streamValidator.validateForCreate(stream);
    stream.setSpecification(handlerService.handleInsert(stream));
    return save(stream);
  }

  @PreAuthorize("hasPermission(#stream, 'UPDATE')")
  public Optional<Stream> update(Stream stream) throws ValidationException {
    val existing = streamView.get(stream.getKey());
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
    return streamView.get(key);
  }

  @PostFilter("hasPermission(filterObject, 'READ')")
  public List<Stream> findAll(Predicate<Stream> filter) {
    return streamView.findAll(filter).collect(toList());
  }

  @PreAuthorize("hasPermission(#stream, 'DELETE')")
  public void delete(Stream stream) {
    handlerService.handleDelete(stream);

    // Find all Processes that have multiple different stream inputs/outputs which also have this Stream as an input or output.
    // These processes would be left invalid if the stream was deleted, so block the delete of this Stream.

    allProcessesForStream(stream)
      .filter(process -> processInputOutputStreamKeySet(process).size() > 1)
      .findAny()
      .ifPresent(process -> {
        throw new IllegalStateException("Cannot delete Stream, Processes depend on it including: " + processKeyString(process));
      });

    // Assuming the above check passed, then cascade deletes to Processes which have ONLY this stream as input/output.
    // These types of Processes would be entirely useless without the Stream and so can be safely deleted.
    // This will cascade to ProcessBinding also.
    allProcessesForStream(stream)
      .filter(process -> processInputOutputStreamKeySet(process).size() == 1)
      .forEach(processService::delete);

    // This will cascade to ConsumerBinding and ProducerBinding also
    streamBindingView
      .findAll(b -> b.getKey().getStreamKey().equals(stream.getKey()))
      .forEach(streamBindingService::delete);

    // Remove producers AFTER consumers - a consumer is nothing without a producer
    consumerView
      .findAll(c -> c.getKey().getStreamKey().equals(stream.getKey()))
      .forEach(consumerService::delete);

    // We have no consumers so we can now remove the producers
    producerView
      .findAll(p -> p.getKey().getStreamKey().equals(stream.getKey()))
      .forEach(producerService::delete);

    streamRepository.delete(stream);

    SchemaKey schemaKey = stream.getSchemaKey();
    boolean schemaReferencedByOtherStreams = streamView.findAll(s -> s.getSchemaKey().equals(schemaKey))
      .anyMatch(s -> !s.getKey().equals(stream.getKey()));

    if (!schemaReferencedByOtherStreams) {
      schemaView.get(schemaKey).ifPresent(schemaService::delete);
    }
  }

  private String processKeyString(Process process) {
    return process.getKey().getDomain() + ":" + process.getKey().getName();
  }

  private Set<StreamKey> processInputOutputStreamKeySet(Process process) {
    return java.util.stream.Stream.concat(
      process.getInputs().stream().map(ProcessInputStream::getStreamKey),
      process.getOutputs().stream().map(ProcessOutputStream::getStreamKey)
    ).collect(java.util.stream.Collectors.toSet());
  }

  private java.util.stream.Stream<Process> allProcessesForStream(Stream stream) {
    return processView.findAll(process ->
      process.getInputs().stream().anyMatch(input -> input.getStreamKey().equals(stream.getKey())) ||
        process.getOutputs().stream().anyMatch(output -> output.getStreamKey().equals(stream.getKey()))
    );
  }
}
