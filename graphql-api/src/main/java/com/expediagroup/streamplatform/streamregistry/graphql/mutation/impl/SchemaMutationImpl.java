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
package com.expediagroup.streamplatform.streamregistry.graphql.mutation.impl;

import static com.expediagroup.streamplatform.streamregistry.graphql.StateHelper.maintainState;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.SchemaService;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SchemaKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.graphql.mutation.SchemaMutation;
import com.expediagroup.streamplatform.streamregistry.model.Schema;

@Component
@RequiredArgsConstructor
public class SchemaMutationImpl implements SchemaMutation {
  private final SchemaService schemaService;

  @Override
  public Schema insert(SchemaKeyInput key, SpecificationInput specification) {
    return schemaService.create(asSchema(key, specification)).get();
  }

  @Override
  public Schema update(SchemaKeyInput key, SpecificationInput specification) {
    return schemaService.update(asSchema(key, specification)).get();
  }

  @Override
  public Schema upsert(SchemaKeyInput key, SpecificationInput specification) {
    return schemaService.upsert(asSchema(key, specification)).get();
  }

  @Override
  public Boolean delete(SchemaKeyInput key) {
    throw new UnsupportedOperationException("delete");
  }

  @Override
  public Schema updateStatus(SchemaKeyInput key, StatusInput status) {
    Schema schema = schemaService.read(key.asSchemaKey()).get();
    schema.setStatus(status.asStatus());
    return schemaService.update(schema).get();
  }

  private Schema asSchema(SchemaKeyInput key, SpecificationInput specification) {
    Schema schema = new Schema();
    schema.setKey(key.asSchemaKey());
    schema.setSpecification(specification.asSpecification());
    maintainState(schema, schemaService.read(schema.getKey()));
    return schema;
  }
}
