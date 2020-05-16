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
package com.expediagroup.streamplatform.streamregistry.state.avro;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Test;

public class AvroObjectSerializerTest {

  @Test
  public void test() {
    ObjectMapper mapper = new ObjectMapper().registerModule(new AvroObjectModule());

    Map<String, Object> fields = new HashMap<>();
    fields.put("stringField", "x");
    fields.put("floatField", 1.0F);
    fields.put("doubleField", 1.0D);
    fields.put("intField", 1);
    fields.put("longField", 1L);
    fields.put("booleanField", true);
    fields.put("nullField", null);
    fields.put("objectField", new AvroObject(Map.of("foo", 1)));
    fields.put("arrayField", new AvroArray(List.of("bar")));
    AvroObject avroObject = new AvroObject(fields);

    ObjectNode objectNode = mapper.convertValue(avroObject, ObjectNode.class);

    System.out.println(objectNode);
  }
}
