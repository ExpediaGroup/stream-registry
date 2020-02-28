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
package com.expediagroup.streamplatform.streamregistry.core.events.listeners;

import java.util.List;
import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEvent;
import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventHandler;
import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventListener;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class StreamNotificationEventListener implements NotificationEventListener<Stream> {
  private static final String IS_CREATING_A_STREAM = "" +
      "event.entity instanceof T(com.expediagroup.streamplatform.streamregistry.model.Stream)" +
      "and event.eventType == T(com.expediagroup.streamplatform.streamregistry.core.events.EventType).CREATE";

  private static final String IS_UPDATING_A_STREAM = "" +
      "event.entity instanceof T(com.expediagroup.streamplatform.streamregistry.model.Stream)" +
      "and event.eventType == T(com.expediagroup.streamplatform.streamregistry.core.events.EventType).UPDATE";


  private static final String IS_DELETING_A_STREAM = "" +
      "event.entity instanceof T(com.expediagroup.streamplatform.streamregistry.model.Stream)" +
      "and event.eventType == T(com.expediagroup.streamplatform.streamregistry.core.events.EventType).DELETE";

  private final List<NotificationEventHandler<Stream>> notificationEventHandlers;

  @Override
  @EventListener(condition = IS_CREATING_A_STREAM)
  public void onCreate(NotificationEvent<Stream> event) {
    log.debug("On update stream event {}", event);
    notificationEventHandlers.parallelStream().forEach(h -> this.handle(h::onCreate, event));
  }

  @Override
  @EventListener(condition = IS_UPDATING_A_STREAM)
  public void onUpdate(NotificationEvent<Stream> event) {
    log.debug("On update stream event {}", event);
    notificationEventHandlers.parallelStream().forEach(h -> this.handle(h::onUpdate, event));
  }

  @Override
  @EventListener(condition = IS_DELETING_A_STREAM)
  public void onDelete(NotificationEvent<Stream> event) {
    log.debug("On delete stream event {}", event);
    notificationEventHandlers.parallelStream().forEach(h -> this.handle(h::onDelete, event));
  }

  private void handle(Consumer<NotificationEvent<Stream>> handle, NotificationEvent<Stream> event) {
    try {
      handle.accept(event);
    } catch (Error e) {
      log.error("Error handling event {}", event, e);
    }
  }
}