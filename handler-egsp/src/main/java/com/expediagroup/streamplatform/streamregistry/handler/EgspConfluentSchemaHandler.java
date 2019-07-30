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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.model.Schema;

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
  public Schema handle(Schema schema, Optional<? extends Schema> existing) {
    if (!schema.getConfiguration().containsKey(SCHEMA_REGISTRY_URL)) {
      Map<String, String> map = new HashMap<>(schema.getConfiguration());
      map.put(SCHEMA_REGISTRY_URL, schemaRegistryUrl);
      schema = schema.withConfiguration(map);
    }
    return schema;
  }
}
