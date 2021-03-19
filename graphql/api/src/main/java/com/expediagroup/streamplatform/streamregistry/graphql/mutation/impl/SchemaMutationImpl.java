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
package com.expediagroup.streamplatform.streamregistry.graphql.mutation.impl;

import static com.expediagroup.streamplatform.streamregistry.graphql.StateHelper.maintainState;

import com.expediagroup.streamplatform.streamregistry.core.views.SchemaView;
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
  private final SchemaView schemaView;

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
    Schema schema = asSchema(key, specification);
    if (!schemaView.get(schema.getKey()).isPresent()) {
      return schemaService.create(schema).get();
    } else {
      return schemaService.update(schema).get();
    }
  }

  @Override
  public Boolean delete(SchemaKeyInput key) {
    throw new UnsupportedOperationException("Please delete the stream associated with this schema instead.");
  }

  @Override
  public Schema updateStatus(SchemaKeyInput key, StatusInput status) {
    Schema schema = schemaView.get(key.asSchemaKey()).get();
    return schemaService.updateStatus(schema, status.asStatus()).get();
  }

  private Schema asSchema(SchemaKeyInput key, SpecificationInput specification) {
    Schema schema = new Schema();
    schema.setKey(key.asSchemaKey());
    schema.setSpecification(specification.asSpecification());
    maintainState(schema, schemaView.get(schema.getKey()));
    return schema;
  }
}
