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

import java.util.concurrent.Future;
import java.util.function.Function;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.apache.avro.specific.SpecificRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEvent;
import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventHandler;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

@Slf4j
@Builder
public class StreamEventHandlerForKafka implements NotificationEventHandler<Stream> {

  @Getter
  @NonNull
  private final String notificationEventsTopic;

  @Getter
  @NonNull
  private final Function<Stream, ?> streamToKeyRecord;

  @Getter
  @NonNull
  private final Function<Stream, ?> streamToValueRecord;

  @Getter
  @NonNull
  private final KafkaTemplate<SpecificRecord, SpecificRecord> kafkaTemplate;

  @Override
  public void onCreate(NotificationEvent<Stream> event) {
    log.info("Pushing create-stream event {} to Kafka", event);
    sendStreamNotificationEvent(event);
  }

  @Override
  public void onUpdate(NotificationEvent<Stream> event) {
    log.info("Pushing update-stream event {} to Kafka", event);
    sendStreamNotificationEvent(event);
  }

  @Override
  public void onDelete(NotificationEvent<Stream> event) {
    log.warn("On delete-stream is not implemented in StreamEventHandlerForKafka. Event {} is going to be ignored", event);
  }

  private Future<SendResult<SpecificRecord, SpecificRecord>> sendStreamNotificationEvent(NotificationEvent<Stream> event) {
    val key = streamToKeyRecord.apply(event.getEntity());
    val value = streamToValueRecord.apply(event.getEntity());

    return kafkaTemplate.send(notificationEventsTopic, (SpecificRecord) key, (SpecificRecord) value);
  }
}