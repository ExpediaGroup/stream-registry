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
package com.expediagroup.streamplatform.streamregistry.core.events.handlers;

import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.Function;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.apache.avro.specific.SpecificRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.support.MessageBuilder;

import com.expediagroup.streamplatform.streamregistry.core.events.EventType;
import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEvent;
import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventHandler;
import com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConstants;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;

@Slf4j
@Builder
public class StreamBindingEventHandlerForKafka implements NotificationEventHandler<StreamBinding> {

  @Getter
  @NonNull
  private final String notificationEventsTopic;

  @Getter
  @NonNull
  private final Function<StreamBinding, ?> streamBindingToKeyRecord;

  @Getter
  @NonNull
  private final Function<StreamBinding, ?> streamBindingToValueRecord;

  @Getter
  @NonNull
  private final KafkaTemplate<SpecificRecord, SpecificRecord> kafkaTemplate;

  @Override
  public void onCreate(NotificationEvent<StreamBinding> event) {
    log.info("Pushing create-stream-binding event {} to Kafka", event);
    sendStreamBindingNotificationEvent(event);
  }

  @Override
  public void onUpdate(NotificationEvent<StreamBinding> event) {
    log.info("Pushing update-stream-binding event {} to Kafka", event);
    sendStreamBindingNotificationEvent(event);
  }

  @Override
  public void onDelete(NotificationEvent<StreamBinding> event) {
    log.warn("On delete-stream-binding is not implemented in StreamEventHandlerForKafka. Event {} is going to be ignored", event);
  }

  private Future<SendResult<SpecificRecord, SpecificRecord>> sendStreamBindingNotificationEvent(NotificationEvent<StreamBinding> event) {
    val key = streamBindingToKeyRecord.apply(event.getEntity());
    val value = streamBindingToValueRecord.apply(event.getEntity());

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
        .setHeader(KafkaHeaders.TOPIC, notificationEventsTopic)
        .setHeader(NotificationEventConstants.NOTIFICATION_TYPE_HEADER.name, eventType)
        .setHeader(NotificationEventConstants.ENTITY_TYPE_HEADER.name, entity)
        .build();

    return kafkaTemplate.send(message);
  }
}