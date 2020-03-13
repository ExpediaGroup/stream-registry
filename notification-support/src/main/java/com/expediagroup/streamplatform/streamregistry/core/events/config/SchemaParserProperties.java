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
package com.expediagroup.streamplatform.streamregistry.core.events.config;

import static com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventUtils.getWarningMessageOnNotDefinedProp;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.CUSTOM_SCHEMA_KEY_PARSER_CLASS_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.CUSTOM_SCHEMA_KEY_PARSER_METHOD_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.CUSTOM_SCHEMA_TYPE_PREFIX;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.CUSTOM_SCHEMA_VALUE_PARSER_CLASS_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.CUSTOM_SCHEMA_VALUE_PARSER_METHOD_PROPERTY;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import lombok.Data;

import org.apache.avro.specific.SpecificRecord;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventUtils;
import com.expediagroup.streamplatform.streamregistry.model.Schema;

@Data
@ConfigurationProperties(prefix = CUSTOM_SCHEMA_TYPE_PREFIX)
public class SchemaParserProperties {
  private Boolean customEnabled;
  private String keyParserClass;
  private String keyParserMethod;
  private String valueParserClass;
  private String valueParserMethod;

  public Function<Schema, ?> buildSchemaToKeyRecord() {
    return Optional.ofNullable(customEnabled)
        .filter(Boolean::booleanValue)
        .map(e -> this.loadKeyParser())
        .orElse(NotificationEventUtils::toAvroKeyRecord);
  }

  public Function<Schema, ?> buildSchemaToValueRecord() {
    return Optional.ofNullable(customEnabled)
        .filter(Boolean::booleanValue)
        .map(e -> this.loadValueParser())
        .orElse(NotificationEventUtils::toAvroValueRecord);
  }

  private <R extends SpecificRecord> Function<Schema, R> loadKeyParser() {
    Objects.requireNonNull(keyParserClass, getWarningMessageOnNotDefinedProp("enabled schema type parser", CUSTOM_SCHEMA_KEY_PARSER_CLASS_PROPERTY));
    Objects.requireNonNull(keyParserMethod, getWarningMessageOnNotDefinedProp("enabled schema type parser", CUSTOM_SCHEMA_KEY_PARSER_METHOD_PROPERTY));

    try {
      return NotificationEventUtils.loadToAvroStaticMethod(keyParserClass, keyParserMethod, Schema.class);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private <R extends SpecificRecord> Function<Schema, R> loadValueParser() {
    Objects.requireNonNull(valueParserClass, getWarningMessageOnNotDefinedProp("enabled schema type parser", CUSTOM_SCHEMA_VALUE_PARSER_CLASS_PROPERTY));
    Objects.requireNonNull(valueParserMethod, getWarningMessageOnNotDefinedProp("enabled schema type parser", CUSTOM_SCHEMA_VALUE_PARSER_METHOD_PROPERTY));

    try {
      return NotificationEventUtils.loadToAvroStaticMethod(valueParserClass, valueParserMethod, Schema.class);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}