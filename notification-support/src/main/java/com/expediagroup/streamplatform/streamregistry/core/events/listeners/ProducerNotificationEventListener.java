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
import com.expediagroup.streamplatform.streamregistry.model.Producer;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProducerNotificationEventListener implements NotificationEventListener<Producer> {
  private static final String IS_CREATING_A_PRODUCER = "" +
      "event.entity instanceof T(com.expediagroup.streamplatform.streamregistry.model.Producer)" +
      "and event.eventType == T(com.expediagroup.streamplatform.streamregistry.core.events.EventType).CREATE";

  private static final String IS_UPDATING_A_PRODUCER = "" +
      "event.entity instanceof T(com.expediagroup.streamplatform.streamregistry.model.Producer)" +
      "and event.eventType == T(com.expediagroup.streamplatform.streamregistry.core.events.EventType).UPDATE";


  private static final String IS_DELETING_A_PRODUCER = "" +
      "event.entity instanceof T(com.expediagroup.streamplatform.streamregistry.model.Producer)" +
      "and event.eventType == T(com.expediagroup.streamplatform.streamregistry.core.events.EventType).DELETE";

  private final List<NotificationEventHandler<Producer>> notificationEventHandlers;

  @Override
  @EventListener(condition = IS_CREATING_A_PRODUCER)
  public void onCreate(NotificationEvent<Producer> event) {
    log.debug("On update producer event {}", event);
    notificationEventHandlers.forEach(h -> this.handle(h::onCreate, event));
  }

  @Override
  @EventListener(condition = IS_UPDATING_A_PRODUCER)
  public void onUpdate(NotificationEvent<Producer> event) {
    log.debug("On update producer event {}", event);
    notificationEventHandlers.forEach(h -> this.handle(h::onUpdate, event));
  }

  @Override
  @EventListener(condition = IS_DELETING_A_PRODUCER)
  public void onDelete(NotificationEvent<Producer> event) {
    log.debug("On delete producer event {}", event);
    notificationEventHandlers.forEach(h -> this.handle(h::onDelete, event));
  }

  private void handle(Consumer<NotificationEvent<Producer>> handle, NotificationEvent<Producer> event) {
    try {
      handle.accept(event);
    } catch (Error e) {
      log.error("Error handling event {}", event, e);
    }
  }
}