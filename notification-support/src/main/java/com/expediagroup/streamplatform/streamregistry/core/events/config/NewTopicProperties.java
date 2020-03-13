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
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.KAFKA_TOPIC_NAME_PROPERTY;
import static com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig.KAFKA_TOPIC_SETUP_PROPERTY;

import java.util.Map;
import java.util.Objects;

import lombok.Data;
import lombok.val;

import com.google.common.base.Preconditions;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = KAFKA_TOPIC_SETUP_PROPERTY)
public class NewTopicProperties {
  private Integer numPartitions;
  private Short replicationFactor;
  private Map<String, String> configs = null;

  public NewTopic buildNewTopic(final String topicName) {
    // We execute this method and its validations only when 'notification.events.kafka.topic.setup' is true
    // during KafkaSetupHandler building. If we use @Validated and its constraints, bean loading will fail even
    // when topic setup is disabled.
    val component = "enabled Kafka topic setup";

    Objects.requireNonNull(topicName, getWarningMessageOnNotDefinedProp(component, KAFKA_TOPIC_NAME_PROPERTY));
    Objects.requireNonNull(numPartitions, getWarningMessageOnNotDefinedProp(component,
        KAFKA_TOPIC_SETUP_PROPERTY.concat(".numPartitions")));
    Objects.requireNonNull(replicationFactor, getWarningMessageOnNotDefinedProp(component,
        KAFKA_TOPIC_SETUP_PROPERTY.concat(".replicationFactor")));

    val gtZeroWarning = " must be greater than zero";

    Preconditions.checkArgument(numPartitions.compareTo(0) > 0,
        KAFKA_TOPIC_SETUP_PROPERTY.concat(".numPartitions").concat(gtZeroWarning));
    Preconditions.checkArgument(replicationFactor.intValue() > 0,
        KAFKA_TOPIC_SETUP_PROPERTY.concat(".replicationFactor").concat(gtZeroWarning));

    return new NewTopic(topicName, numPartitions, replicationFactor).configs(configs);
  }
}
