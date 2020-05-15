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

import static com.fasterxml.jackson.core.JsonToken.END_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.END_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

@Slf4j
class AvroObjectDeserializer extends JsonDeserializer<AvroObject> {
  @Override
  public AvroObject deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    if (p.currentToken() != START_OBJECT) {
      throw new IllegalStateException("Unexpected start token: " + p.currentToken());
    }
    return (AvroObject) deserialize(p);
  }

  Object deserialize(JsonParser p) throws IOException {
    while (p.currentToken() != null) {
      log.trace("token: {}, name: {}, value: {}", p.currentToken(), p.getCurrentName(), p.getText());
      switch (p.currentToken()) {
        case VALUE_STRING:
          return p.getText();
        case VALUE_NUMBER_FLOAT:
          return p.getDoubleValue();
        case VALUE_NUMBER_INT:
          return p.getLongValue();
        case VALUE_FALSE:
        case VALUE_TRUE:
          return p.getBooleanValue();
        case VALUE_NULL:
          return null;
        case START_OBJECT:
          return deserializeObject(p);
        case START_ARRAY:
          return deserializeArray(p);
        case FIELD_NAME:
        case END_OBJECT:
        case END_ARRAY:
          //ignore
          break;
        default:
          throw new IllegalStateException("Unexpected token: " + p.currentToken());
      }
      p.nextToken();
    }
    throw new IllegalStateException("Unexpectedly finished processing stream");
  }

  private AvroObject deserializeObject(JsonParser p) throws IOException {
    var fields = new HashMap<String, Object>();
    process(p, END_OBJECT, fields::put);
    return new AvroObject(fields);
  }

  private AvroArray deserializeArray(JsonParser p) throws IOException {
    var items = new ArrayList<Object>();
    process(p, END_ARRAY, (name, item) -> items.add(item));
    return new AvroArray(items);
  }

  private void process(JsonParser p, JsonToken breakOn, BiConsumer<String, Object> consumer) throws IOException {
    while (true) {
      p.nextToken();
      if (p.currentToken() == breakOn) {
        break;
      }
      consumer.accept(p.currentName(), deserialize(p));
    }
  }
}
