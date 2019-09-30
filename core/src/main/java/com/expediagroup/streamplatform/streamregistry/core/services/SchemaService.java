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

import com.expediagroup.streamplatform.streamregistry.core.augmentors.SchemaAugmentor;
import com.expediagroup.streamplatform.streamregistry.core.filters.Filter;
import com.expediagroup.streamplatform.streamregistry.core.repositories.SchemaRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.SchemaValidator;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;

@Component
public class SchemaService {

  SchemaRepository schemaRepository;
  SchemaValidator schemaValidator;
  SchemaAugmentor schemaAugmentor;

  public SchemaService(
      SchemaRepository schemaRepository,
      SchemaValidator schemaValidator,
      SchemaAugmentor schemaAugmentor) {
    this.schemaRepository = schemaRepository;
    this.schemaValidator = schemaValidator;
    this.schemaAugmentor = schemaAugmentor;
  }

  public Optional<Schema> create(Schema schema) throws ValidationException {
    if (schemaRepository.findById(schema.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    schemaValidator.validateForCreate(schema);
    schema = schemaAugmentor.augmentForCreate(schema);
    return Optional.ofNullable(schemaRepository.save(schema));
  }

  public Optional<Schema> read(SchemaKey key) {
    return schemaRepository.findById(key);
  }

  public Iterable<Schema> readAll() {
    return schemaRepository.findAll();
  }

  public Optional<Schema> update(Schema schema) throws ValidationException {
    Optional<Schema> existing = schemaRepository.findById(schema.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    schema = schemaAugmentor.augmentForUpdate(schema, existing.get());
    schemaValidator.validateForUpdate(schema, existing.get());
    return Optional.ofNullable(schemaRepository.save(schema));
  }

  public Optional<Schema> upsert(Schema schema) throws ValidationException {
    return !schemaRepository.findById(schema.getKey()).isPresent() ?
        create(schema) :
        update(schema);
  }

  public void delete(Schema schema) {
  }

  public Iterable<Schema> findAll(Filter<Schema> filter) {
    return stream(schemaRepository.findAll().spliterator(), false)
        .filter(r -> filter.matches(r))
        .collect(Collectors.toList());
  }
}