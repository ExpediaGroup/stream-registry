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
package com.expediagroup.streamplatform.streamregistry.handler;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Specification;

@Component
public class EgspConfluentSchemaHandler implements Handler<Schema> {

  public static final String SCHEMA_REGISTRY_URL = "schema_registry_url";
  private final String schemaRegistryUrl;

  public EgspConfluentSchemaHandler(
      @Value("${schema.registry.url}") String schemaRegistryUrl) {
    this.schemaRegistryUrl = schemaRegistryUrl;
  }

  @Override
  public String type() {
    return "egsp.confluent";
  }

  @Override
  public Class target() {
    return Schema.class;
  }

  @Override
  public Specification handleInsert(Schema schema) throws HandlerException {
    return handle(schema);
  }

  @Override
  public Specification handleUpdate(Schema schema, Schema existing) throws HandlerException {
    return handle(schema);
  }

  public Specification handle(Schema schema) throws HandlerException {
    ObjectNode configuration = schema.getSpecification().getConfiguration();
    if (!configuration.has(SCHEMA_REGISTRY_URL)) {
      configuration.put(SCHEMA_REGISTRY_URL, schemaRegistryUrl);
    }
    schema.getSpecification().setConfiguration(configuration);
    return schema.getSpecification();
  }
}
