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

import java.io.IOException;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@Slf4j
class AvroObjectSerializer extends StdSerializer<AvroObject> {
  AvroObjectSerializer() {
    super(AvroObject.class);
  }

  @Override
  public void serialize(AvroObject object, JsonGenerator g, SerializerProvider serializers) throws IOException {
    serializeObject(object, g);
  }

  private void serialize(Object value, JsonGenerator g) throws IOException {
    if (value instanceof AvroObject) {
      serializeObject((AvroObject) value, g);
    } else if (value instanceof AvroArray) {
      serializeArray((AvroArray) value, g);
    } else if (value instanceof String) {
      g.writeString((String) value);
    } else if (value instanceof Double) {
      g.writeNumber((double) value);
    } else if (value instanceof Float) {
      g.writeNumber((float) value);
    } else if (value instanceof Long) {
      g.writeNumber((long) value);
    } else if (value instanceof Integer) {
      g.writeNumber((int) value);
    } else if (value instanceof Boolean) {
      g.writeBoolean((boolean) value);
    } else if (value == null) {
      g.writeNull();
    }
  }

  private void serializeObject(AvroObject object, JsonGenerator g) throws IOException {
    g.writeStartObject();
    for (Map.Entry<String, Object> entry : object.getValue().entrySet()) {
      g.writeFieldName(entry.getKey());
      serialize(entry.getValue(), g);
    }
    g.writeEndObject();
  }

  private void serializeArray(AvroArray array, JsonGenerator g) throws IOException {
    g.writeStartArray();
    for (Object value : array.getValue()) {
      serialize(value, g);
    }
    g.writeEndArray();
  }
}
