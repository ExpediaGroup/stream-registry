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

import static com.expediagroup.streamplatform.streamregistry.handler.EgspConfluentSchemaHandler.SCHEMA_REGISTRY_URL;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.repository.kafka.Config;

public class EgspConfluentSchemaHandlerTest {

  private static final String URL = "some_url";

  private Config config;
  private EgspConfluentSchemaHandler handler;

  @Before
  public void before() {
    config = Config.builder().schemaRegistryUrl(URL).build();
    handler = new EgspConfluentSchemaHandler(config);
  }

  @Test
  public void type() {
    assertEquals("egsp.confluent", handler.type());
  }

  @Test
  public void handle() {
    assertEquals("some_url", createAndHandle(null).getConfiguration().get(SCHEMA_REGISTRY_URL));
  }

  @Test
  public void handleWithExistingURL() {
    assertEquals("some_existing_url", createAndHandle("some_existing_url").getConfiguration().get(SCHEMA_REGISTRY_URL));
  }

  private Schema createAndHandle(String existingUrl) {
    Schema schema = createSchema(existingUrl);
    return handler.handle(schema, Optional.empty());
  }

  private Schema createSchema(String existingUrl) {
    Map configuration = new HashMap<>();
    if (existingUrl != null) {
      configuration.put(SCHEMA_REGISTRY_URL, existingUrl);
    }
    return Schema.builder().tags(new HashMap<>()).configuration(configuration).build();
  }
}