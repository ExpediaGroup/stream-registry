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
import com.expediagroup.streamplatform.streamregistry.core.validators.SchemaValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.repository.SchemaRepository;

@Component
@RequiredArgsConstructor
public class SchemaService {
  private final HandlerService handlerService;
  private final SchemaValidator schemaValidator;
  private final SchemaRepository schemaRepository;

  @PreAuthorize("hasPermission(#schema, 'CREATE')")
  public Optional<Schema> create(Schema schema) throws ValidationException {
    if (read(schema.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    schemaValidator.validateForCreate(schema);
    schema.setSpecification(handlerService.handleInsert(schema));
    return save(schema);
  }

  @PreAuthorize("hasPermission(#schema, 'UPDATE')")
  public Optional<Schema> update(Schema schema) throws ValidationException {
    var existing = read(schema.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update " + schema.getKey().getName() + " because it doesn't exist");
    }
    schemaValidator.validateForUpdate(schema, existing.get());
    schema.setSpecification(handlerService.handleUpdate(schema, existing.get()));
    return save(schema);
  }

  @PreAuthorize("hasPermission(#schemaKey, 'UPDATE_STATUS')")
  public Optional<Schema> updateStatus(SchemaKey schemaKey, Status status) {
    Schema schema = read(schemaKey).get();
    schema.setStatus(status);
    return save(schema);
  }

  private Optional<Schema> save(Schema schema) {
    schema = schemaRepository.save(schema);
    return Optional.ofNullable(schema);
  }

  public Optional<Schema> upsert(Schema schema) throws ValidationException {
    return !read(schema.getKey()).isPresent() ? create(schema) : update(schema);
  }

  public Optional<Schema> read(SchemaKey key) {
    return schemaRepository.findById(key);
  }

  public List<Schema> findAll(Predicate<Schema> filter) {
    return schemaRepository.findAll().stream().filter(filter).collect(toList());
  }

  @PreAuthorize("hasPermission(#schema, 'DELETE')")
  public void delete(Schema schema) {
    throw new UnsupportedOperationException();
  }

  public boolean exists(SchemaKey key) {
    return read(key).isPresent();
  }
}
