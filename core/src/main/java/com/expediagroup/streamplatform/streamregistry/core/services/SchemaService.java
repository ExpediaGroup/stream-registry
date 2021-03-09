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
import com.expediagroup.streamplatform.streamregistry.core.validators.SchemaValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.repository.SchemaRepository;

@Component
@RequiredArgsConstructor
public class SchemaService {
  private final HandlerService handlerService;
  private final SchemaValidator schemaValidator;
  private final SchemaRepository schemaRepository;
  private final StreamService streamService;

  @PreAuthorize("hasPermission(#schema, 'CREATE')")
  public Optional<Schema> create(Schema schema) throws ValidationException {
    if (unsecuredGet(schema.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    schemaValidator.validateForCreate(schema);
    schema.setSpecification(handlerService.handleInsert(schema));
    return save(schema);
  }

  @PreAuthorize("hasPermission(#schema, 'UPDATE')")
  public Optional<Schema> update(Schema schema) throws ValidationException {
    val existing = unsecuredGet(schema.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update " + schema.getKey().getName() + " because it doesn't exist");
    }
    schemaValidator.validateForUpdate(schema, existing.get());
    schema.setSpecification(handlerService.handleUpdate(schema, existing.get()));
    return save(schema);
  }

  @PreAuthorize("hasPermission(#schema, 'UPDATE_STATUS')")
  public Optional<Schema> updateStatus(Schema schema, Status status) {
    schema.setStatus(status);
    return save(schema);
  }

  private Optional<Schema> save(Schema schema) {
    schema = schemaRepository.save(schema);
    return Optional.ofNullable(schema);
  }

  @PostAuthorize("returnObject.isPresent() ? hasPermission(returnObject, 'READ') : true")
  public Optional<Schema> get(SchemaKey key) {
    return unsecuredGet(key);
  }

  public Optional<Schema> unsecuredGet(SchemaKey key) {
    return schemaRepository.findById(key);
  }

  @PostFilter("hasPermission(filterObject, 'READ')")
  public List<Schema> findAll(Predicate<Schema> filter) {
    return schemaRepository.findAll().stream().filter(filter).collect(toList());
  }

  @PreAuthorize("hasPermission(#schema, 'DELETE')")
  public void delete(Schema schema) {
    handlerService.handleDelete(schema);
    List<Stream> streams = streamService.findAll(schema.getKey());
    if(streams.isEmpty()) {
      schemaRepository.delete(schema);
    } else {
    }
  }

  public boolean exists(SchemaKey key) {
    return unsecuredGet(key).isPresent();
  }
}
