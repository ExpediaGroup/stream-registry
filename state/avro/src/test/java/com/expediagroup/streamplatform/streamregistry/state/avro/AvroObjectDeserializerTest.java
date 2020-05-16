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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Test;

public class AvroObjectDeserializerTest {

  @Test
  public void test() {
    ObjectMapper mapper = new ObjectMapper()
        .registerModule(new AvroObjectModule());

    ObjectNode objectNode = mapper.createObjectNode()
        .put("stringField", "x")
        .put("floatField", 1.0F)
        .put("doubleField", 1.0D)
        .put("intField", 1)
        .put("longField", 1L)
        .put("booleanField", true)
        .putNull("nullField")
        .<ObjectNode>set("objectField", mapper.createObjectNode().put("foo", 1))
        .<ObjectNode>set("arrayField", mapper.createArrayNode().add("bar"));

    AvroObject avroObjectNode = mapper.convertValue(objectNode, AvroObject.class);
    System.out.println(avroObjectNode);
  }
}
