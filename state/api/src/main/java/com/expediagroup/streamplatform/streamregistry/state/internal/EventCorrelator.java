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
package com.expediagroup.streamplatform.streamregistry.state.internal;

import java.util.concurrent.CompletableFuture;

import com.expediagroup.streamplatform.streamregistry.state.EventReceiver;
import com.expediagroup.streamplatform.streamregistry.state.EventSender;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;

/**
 * Provides a mechanism for an {@link EventSender} to coordinate with an {@link EventReceiver}. This allows
 * it to be able to wait until the receiver confirms receipt before completing the {@link EventSender#send(Event)}
 * call. Intended for use internally within Stream Registry itself only.
 */
public interface EventCorrelator {
  String CORRELATION_ID = "correlationId";

  /**
   * Register a {@link CompletableFuture}. The {@link EventSender} calls this method and then passes the
   * correlation id along with the message.
   *
   * @param future a future
   * @return a correlation id.
   */
  String register(CompletableFuture<Void> future);

  /**
   * The {@link EventReceiver} calls this method when the event has been successfully received.
   *
   * @param correlationId the correlation id received along with the event.
   */
  void received(String correlationId);

  /**
   * The {@link EventSender} calls this method if it was unable to send the event.
   *
   * @param correlationId the correlation id associated with the event.
   * @param e
   */
  void failed(String correlationId, Exception e);
}
