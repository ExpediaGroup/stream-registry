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

import static com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventHandler.sendEntityNotificationEvent;

import java.util.concurrent.Future;
import java.util.function.Function;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.apache.avro.specific.SpecificRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEvent;
import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventHandler;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;

@Slf4j
@Builder
public class ConsumerEventHandlerForKafka implements NotificationEventHandler<Consumer> {

  @Getter
  @NonNull
  private final String notificationEventsTopic;

  @Getter
  @NonNull
  private final Function<Consumer, ?> consumerToKeyRecord;

  @Getter
  @NonNull
  private final Function<Consumer, ?> consumerToValueRecord;

  @Getter
  @NonNull
  private final KafkaTemplate<SpecificRecord, SpecificRecord> kafkaTemplate;

  @Override
  public void onCreate(NotificationEvent<Consumer> event) {
    log.info("Pushing create-consumer event {} to Kafka", event);
    sendConsumerNotificationEvent(event);
  }

  @Override
  public void onUpdate(NotificationEvent<Consumer> event) {
    log.info("Pushing update-consumer event {} to Kafka", event);
    sendConsumerNotificationEvent(event);
  }

  @Override
  public void onDelete(NotificationEvent<Consumer> event) {
    log.warn("On delete-consumer is not implemented in ConsumerEventHandlerForKafka. Event {} is going to be ignored", event);
  }

  private Future<SendResult<SpecificRecord, SpecificRecord>> sendConsumerNotificationEvent(NotificationEvent<Consumer> event) {
    return sendEntityNotificationEvent(
        consumerToKeyRecord,
        consumerToValueRecord,
        kafkaTemplate::send,
        notificationEventsTopic,
        event
    );
  }
}