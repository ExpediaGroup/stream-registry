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

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.DataToModel;
import com.expediagroup.streamplatform.streamregistry.ModelToData;
import com.expediagroup.streamplatform.streamregistry.core.events.EventType;
import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventEmitter;
import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.repositories.SchemaRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.SchemaValidator;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SchemaService {
  private final HandlerService handlerService;
  private final SchemaValidator schemaValidator;
  private final SchemaRepository schemaRepository;
  private final NotificationEventEmitter<Schema> schemaServiceEventEmitter;

  public Optional<Schema> create(Schema schema) throws ValidationException {
    com.expediagroup.streamplatform.streamregistry.data.Schema data =
        ModelToData.convertSchema(schema);

    if (schemaRepository.findById(data.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    schemaValidator.validateForCreate(schema);
    data.setSpecification(handlerService.handleInsert(ModelToData.convertSchema(schema)));
    Schema out = DataToModel.convert(schemaRepository.save(data));
    schemaServiceEventEmitter.emitEventOnProcessedEntity(EventType.CREATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Schema> read(SchemaKey key) {
    Optional<com.expediagroup.streamplatform.streamregistry.data.Schema> data =
        schemaRepository.findById(ModelToData.convertSchemaKey(key));
    return data.isPresent() ? Optional.of(DataToModel.convert(data.get())) : Optional.empty();
  }

  public Iterable<Schema> readAll() {
    ArrayList out = new ArrayList();
    for (com.expediagroup.streamplatform.streamregistry.data.Schema schema : schemaRepository.findAll()) {
      out.add(DataToModel.convert(schema));
    }
    return out;
  }

  public Optional<Schema> update(Schema schema) throws ValidationException {
    com.expediagroup.streamplatform.streamregistry.data.Schema schemaData =
        ModelToData.convertSchema(schema);

    Optional<com.expediagroup.streamplatform.streamregistry.data.Schema> existing =
        schemaRepository.findById(schemaData.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    schemaValidator.validateForUpdate(schema, DataToModel.convert(existing.get()));
    schemaData.setSpecification(handlerService.handleInsert(schemaData));
    Schema out = DataToModel.convert(schemaRepository.save(schemaData));
    schemaServiceEventEmitter.emitEventOnProcessedEntity(EventType.UPDATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Schema> upsert(Schema schema) throws ValidationException {

    com.expediagroup.streamplatform.streamregistry.data.Schema schemaData =
        ModelToData.convertSchema(schema);

    return !schemaRepository.findById(schemaData.getKey()).isPresent() ?
        create(schema) :
        update(schema);
  }

  public void delete(Schema schema) {
    throw new UnsupportedOperationException();
  }

  public Iterable<Schema> findAll(Predicate<Schema> filter) {
    return schemaRepository.findAll().stream().map(d -> DataToModel.convert(d)).filter(filter).collect(toList());
  }

  public boolean exists(SchemaKey key) {
    return read(key).isEmpty();
  }

  @Deprecated
  public void validateSchemaExists(SchemaKey key) {
    if (!exists(key)) {
      throw new ValidationException("Schema does not exist");
    }
  }
}
