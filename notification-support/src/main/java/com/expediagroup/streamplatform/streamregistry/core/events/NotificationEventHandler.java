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
package com.expediagroup.streamplatform.streamregistry.core.events;

import java.util.Optional;
import java.util.function.Function;

import lombok.val;

import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConstants;

public interface NotificationEventHandler<T> {
  void onCreate(NotificationEvent<T> event);

  void onUpdate(NotificationEvent<T> event);

  void onDelete(NotificationEvent<T> event);

  static <T, V> T sendEntityNotificationEvent(
      Function<V, ?> entityToKeyRecord,
      Function<V, ?> entityToValueRecord,
      Function<Message<?>, T> sendMessage,
      String topic,
      NotificationEvent<V> event
  ) {
    val key = entityToKeyRecord.apply(event.getEntity());
    val value = entityToValueRecord.apply(event.getEntity());

    val eventType = Optional.ofNullable(event.getEventType())
        .map(EventType::toString)
        .orElse(NotificationEventConstants.NOTIFICATION_TYPE_HEADER.defaultValue);

    val entity = Optional.ofNullable(event.getEntity())
        .map(Object::getClass)
        .map(Class::getSimpleName)
        .map(String::toUpperCase)
        .orElse(NotificationEventConstants.ENTITY_TYPE_HEADER.defaultValue);

    val message = MessageBuilder
        .withPayload(value)
        .setHeader(KafkaHeaders.MESSAGE_KEY, key)
        .setHeader(KafkaHeaders.TOPIC, topic)
        .setHeader(NotificationEventConstants.NOTIFICATION_TYPE_HEADER.name, eventType)
        .setHeader(NotificationEventConstants.ENTITY_TYPE_HEADER.name, entity)
        .build();

    return sendMessage.apply(message);
  }
}