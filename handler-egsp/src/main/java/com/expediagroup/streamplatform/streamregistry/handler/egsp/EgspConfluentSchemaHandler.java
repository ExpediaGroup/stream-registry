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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.handler.Handler;
import com.expediagroup.streamplatform.streamregistry.model.Configuration;
import com.expediagroup.streamplatform.streamregistry.model.Schema;

@Component
public class EgspConfluentSchemaHandler implements Handler<Schema> {
  public static final String TYPE = "egsp.confluent.alpha";
  public static final String KEY_SCHEMA_SUBJECT = "key.schema.subject";
  public static final String VALUE_SCHEMA_SUBJECTS = "value.schema.subjects";
  private static final String ERROR_TEMPLATE = "EGSP Schema configuration must contain %s";

  @Override
  public String type() {
    return TYPE;
  }

  @Override
  public Schema handle(Schema schema, Optional<? extends Schema> existing) {
    Configuration configuration = schema.getConfiguration();
    Map<String, String> properties = configuration.getProperties();

    String keySubject = properties.get(KEY_SCHEMA_SUBJECT);
    checkNotNull(keySubject, ERROR_TEMPLATE, KEY_SCHEMA_SUBJECT);

    String valueSubjectsString = properties.get(VALUE_SCHEMA_SUBJECTS);
    checkNotNull(valueSubjectsString, ERROR_TEMPLATE, VALUE_SCHEMA_SUBJECTS);
    List<String> valueSubjects = Arrays.asList(valueSubjectsString.split(","));

    existing.ifPresent(e -> {
      Map<String, String> existingProperties = e.getConfiguration().getProperties();

      String existingKeySubject = existingProperties.get(KEY_SCHEMA_SUBJECT);
      checkArgument(Objects.equals(keySubject, existingKeySubject), "Key subject cannot be changed.");

      String existingValueSubjectsString = existingProperties.get(VALUE_SCHEMA_SUBJECTS);
      List<String> existingValueSubjects = Arrays.asList(existingValueSubjectsString.split(","));
      checkArgument(valueSubjects.containsAll(existingValueSubjects), "Existing value subjects cannot be removed.");
    });

    //TODO verify key and value schemas exist in the schema registry?
    //TODO define schema egsp.confluent business rules

    return schema;
  }
}
