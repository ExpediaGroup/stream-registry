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
package com.expediagroup.streamplatform.streamregistry.handler.egsp;

import static com.expediagroup.streamplatform.streamregistry.handler.egsp.EgspConfluentSchemaHandler.KEY_SCHEMA_SUBJECT;
import static com.expediagroup.streamplatform.streamregistry.handler.egsp.EgspConfluentSchemaHandler.TYPE;
import static com.expediagroup.streamplatform.streamregistry.handler.egsp.EgspConfluentSchemaHandler.VALUE_SCHEMA_SUBJECTS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.model.Configuration;
import com.expediagroup.streamplatform.streamregistry.model.Schema;

public class EgspConfluentSchemaHandlerTest {
  private final EgspConfluentSchemaHandler underTest = new EgspConfluentSchemaHandler();

  @Test
  public void updateIdentical() {
    Schema schema = Schema
        .builder()
        .name("name")
        .owner("owner")
        .description("description")
        .tags(Map.of("key", "value"))
        .configuration(Configuration
            .builder()
            .type(TYPE)
            .properties(Map.of(
                KEY_SCHEMA_SUBJECT, "topic-key",
                VALUE_SCHEMA_SUBJECTS, "topic-value1,topic-value2"
            ))
            .build())
        .domain("domain")
        .build();

    Optional existing = Optional.of(schema);

    Schema result = underTest.handle(schema, existing);

    assertThat(result, is(schema));
  }

  @Test(expected = NullPointerException.class)
  public void missingKeySchema() {
    Schema schema = Schema
        .builder()
        .name("name")
        .owner("owner")
        .description("description")
        .tags(Map.of("key", "value"))
        .configuration(Configuration
            .builder()
            .type(TYPE)
            .properties(Map.of(
                VALUE_SCHEMA_SUBJECTS, "topic-value1,topic-value2"
            ))
            .build())
        .domain("domain")
        .build();

    Optional existing = Optional.empty();

    underTest.handle(schema, existing);
  }

  @Test(expected = NullPointerException.class)
  public void missingValueSchemas() {
    Schema schema = Schema
        .builder()
        .name("name")
        .owner("owner")
        .description("description")
        .tags(Map.of("key", "value"))
        .configuration(Configuration
            .builder()
            .type(TYPE)
            .properties(Map.of(
                KEY_SCHEMA_SUBJECT, "topic-key"
            ))
            .build())
        .domain("domain")
        .build();

    Optional existing = Optional.empty();

    underTest.handle(schema, existing);
  }

  @Test(expected = IllegalArgumentException.class)
  public void updateDifferentKey() {
    Schema schema = Schema
        .builder()
        .name("name")
        .owner("owner")
        .description("description")
        .tags(Map.of("key", "value"))
        .configuration(Configuration
            .builder()
            .type(TYPE)
            .properties(Map.of(
                KEY_SCHEMA_SUBJECT, "topic-key2",
                VALUE_SCHEMA_SUBJECTS, "topic-value1,topic-value2"
            ))
            .build())
        .domain("domain")
        .build();

    Optional existing = Optional.of(Schema
        .builder()
        .name("name")
        .owner("owner")
        .description("description")
        .tags(Map.of("key", "value"))
        .configuration(Configuration
            .builder()
            .type(TYPE)
            .properties(Map.of(
                KEY_SCHEMA_SUBJECT, "topic-key1",
                VALUE_SCHEMA_SUBJECTS, "topic-value1,topic-value2"
            ))
            .build())
        .domain("domain")
        .build());

    underTest.handle(schema, existing);
  }

  @Test(expected = IllegalArgumentException.class)
  public void updateRemoveValue() {
    Schema schema = Schema
        .builder()
        .name("name")
        .owner("owner")
        .description("description")
        .tags(Map.of("key", "value"))
        .configuration(Configuration
            .builder()
            .type(TYPE)
            .properties(Map.of(
                KEY_SCHEMA_SUBJECT, "topic-key",
                VALUE_SCHEMA_SUBJECTS, "topic-value1"
            ))
            .build())
        .domain("domain")
        .build();

    Optional existing = Optional.of(Schema
        .builder()
        .name("name")
        .owner("owner")
        .description("description")
        .tags(Map.of("key", "value"))
        .configuration(Configuration
            .builder()
            .type(TYPE)
            .properties(Map.of(
                KEY_SCHEMA_SUBJECT, "topic-key",
                VALUE_SCHEMA_SUBJECTS, "topic-value1,topic-value2"
            ))
            .build())
        .domain("domain")
        .build());

    underTest.handle(schema, existing);
  }

  @Test
  public void updateAddValue() {
    Schema schema = Schema
        .builder()
        .name("name")
        .owner("owner")
        .description("description")
        .tags(Map.of("key", "value"))
        .configuration(Configuration
            .builder()
            .type(TYPE)
            .properties(Map.of(
                KEY_SCHEMA_SUBJECT, "topic-key",
                VALUE_SCHEMA_SUBJECTS, "topic-value1,topic-value2,topic-value3"
            ))
            .build())
        .domain("domain")
        .build();

    Optional existing = Optional.of(Schema
        .builder()
        .name("name")
        .owner("owner")
        .description("description")
        .tags(Map.of("key", "value"))
        .configuration(Configuration
            .builder()
            .type(TYPE)
            .properties(Map.of(
                KEY_SCHEMA_SUBJECT, "topic-key",
                VALUE_SCHEMA_SUBJECTS, "topic-value1,topic-value2"
            ))
            .build())
        .domain("domain")
        .build());

    Schema result = underTest.handle(schema, existing);

    assertThat(result, is(schema));
  }
}
