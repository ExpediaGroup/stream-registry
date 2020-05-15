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
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.CUSTOM_CONSUMER_KEY_PARSER_CLASS_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.CUSTOM_CONSUMER_KEY_PARSER_METHOD_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.CUSTOM_CONSUMER_TYPE_PREFIX;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.CUSTOM_CONSUMER_VALUE_PARSER_CLASS_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.CUSTOM_CONSUMER_VALUE_PARSER_METHOD_PROPERTY;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import lombok.Data;

import org.apache.avro.specific.SpecificRecord;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.expediagroup.streamplatform.streamregistry.core.events.ConsumerNotificationEventUtils;
import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventUtils;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;

@Data
@ConfigurationProperties(prefix = CUSTOM_CONSUMER_TYPE_PREFIX)
public class ConsumerParserProperties {
  private Boolean customEnabled;
  private String keyParserClass;
  private String keyParserMethod;
  private String valueParserClass;
  private String valueParserMethod;

  public Function<Consumer, ?> buildConsumerToKeyRecord() {
    return Optional.ofNullable(customEnabled)
        .filter(Boolean::booleanValue)
        .map(e -> this.loadKeyParser())
        .orElse(ConsumerNotificationEventUtils::toAvroKeyRecord);
  }

  public Function<Consumer, ?> buildConsumerToValueRecord() {
    return Optional.ofNullable(customEnabled)
        .filter(Boolean::booleanValue)
        .map(e -> this.loadValueParser())
        .orElse(ConsumerNotificationEventUtils::toAvroValueRecord);
  }

  private <R extends SpecificRecord> Function<Consumer, R> loadKeyParser() {
    Objects.requireNonNull(keyParserClass, getWarningMessageOnNotDefinedProp("enabled producer type parser", CUSTOM_CONSUMER_KEY_PARSER_CLASS_PROPERTY));
    Objects.requireNonNull(keyParserMethod, getWarningMessageOnNotDefinedProp("enabled producer type parser", CUSTOM_CONSUMER_KEY_PARSER_METHOD_PROPERTY));

    try {
      return NotificationEventUtils.loadToAvroStaticMethod(keyParserClass, keyParserMethod, Consumer.class);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private <R extends SpecificRecord> Function<Consumer, R> loadValueParser() {
    Objects.requireNonNull(valueParserClass, getWarningMessageOnNotDefinedProp("enabled producer type parser", CUSTOM_CONSUMER_VALUE_PARSER_CLASS_PROPERTY));
    Objects.requireNonNull(valueParserMethod, getWarningMessageOnNotDefinedProp("enabled producer type parser", CUSTOM_CONSUMER_VALUE_PARSER_METHOD_PROPERTY));

    try {
      return NotificationEventUtils.loadToAvroStaticMethod(valueParserClass, valueParserMethod, Consumer.class);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}