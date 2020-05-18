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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Test;

public class AvroObjectModuleTest {

  private final ObjectMapper mapper = new ObjectMapper()
      .registerModule(new AvroObjectModule());

  @Test
  public void deserialize() {
    var objectNode = mapper.createObjectNode()
        .put("stringField", "x")
        .put("floatField", 1.0F)
        .put("doubleField", 1.0D)
        .put("intField", 1)
        .put("longField", 1L)
        .put("booleanField", true)
        .putNull("nullField")
        .<ObjectNode>set("objectField", mapper.createObjectNode().put("foo", 1))
        .<ObjectNode>set("arrayField", mapper.createArrayNode().add("bar"));

    var result = mapper.convertValue(objectNode, AvroObject.class);

    var fields = result.getValue();
    assertThat(fields.get("stringField"), is("x"));
    assertThat(fields.get("floatField"), is(1.0D));
    assertThat(fields.get("doubleField"), is(1.0D));
    assertThat(fields.get("intField"), is(1L));
    assertThat(fields.get("longField"), is(1L));
    assertThat(fields.get("booleanField"), is(true));
    assertThat(fields.containsKey("nullField"), is(true));
    assertThat(fields.get("nullField"), is(nullValue()));

    var objectField = (AvroObject) fields.get("objectField");
    assertThat(objectField.getValue().size(), is(1));
    assertThat(objectField.getValue().get("foo"), is(1L));

    var arrayField = (AvroArray) fields.get("arrayField");
    assertThat(arrayField.getValue().size(), is(1));
    assertThat(arrayField.getValue().get(0), is("bar"));
  }

  @Test(expected = IllegalStateException.class)
  public void deserializeNotObject() {
    mapper.convertValue(1, AvroObject.class);
  }

  @Test
  public void serialize() {
    var fields = new HashMap<String, Object>();
    fields.put("stringField", "x");
    fields.put("floatField", 1.0D);
    fields.put("doubleField", 1.0D);
    fields.put("intField", 1L);
    fields.put("longField", 1L);
    fields.put("booleanField", true);
    fields.put("nullField", null);
    fields.put("objectField", new AvroObject(Map.of("foo", 1L)));
    fields.put("arrayField", new AvroArray(List.of("bar")));
    var avroObject = new AvroObject(fields);

    var result = mapper.convertValue(avroObject, ObjectNode.class);

    assertThat(result, is(mapper.createObjectNode()
        .put("stringField", "x")
        .put("floatField", 1.0D)
        .put("doubleField", 1.0D)
        .put("intField", 1L)
        .put("longField", 1L)
        .put("booleanField", true)
        .putNull("nullField")
        .<ObjectNode>set("objectField", mapper.createObjectNode().put("foo", 1L))
        .<ObjectNode>set("arrayField", mapper.createArrayNode().add("bar"))));
  }
}
