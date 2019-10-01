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

import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Before;
import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.scalars.ObjectNodeMapper;

public class EgspConfluentSchemaHandlerTest {
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final String URL = "some_url";

  private EgspConfluentSchemaHandler underTest;

  @Before
  public void before() {
    underTest = new EgspConfluentSchemaHandler(URL);
  }

  @Test
  public void type() {
    assertEquals("egsp.confluent", underTest.type());
  }

  @Test
  public void handle() throws HandlerException {
    assertEquals("some_url", createAndHandle(null).getSpecification().getConfiguration().get(SCHEMA_REGISTRY_URL).asText());
  }

  @Test
  public void handleWithExistingURL() throws HandlerException {
    assertEquals("some_existing_url", createAndHandle("some_existing_url").getSpecification().getConfiguration().get(SCHEMA_REGISTRY_URL).asText());
  }

  private Schema createAndHandle(String existingUrl) throws HandlerException {
    Schema schema = createSchema(existingUrl);
    schema.setSpecification(underTest.handleInsert(schema));
    return schema;
  }

  private Schema createSchema(String existingUrl) {
    ObjectNode configuration = mapper.createObjectNode();
    if (existingUrl != null) {
      configuration.put(SCHEMA_REGISTRY_URL, existingUrl);
    }

    Specification specification=new Specification();
    specification.setTags(new ArrayList<>());
    specification.setConfigJson(ObjectNodeMapper.serialise(configuration));

    Schema schema=new Schema();
    schema.setSpecification(specification);

    return schema;
  }
}