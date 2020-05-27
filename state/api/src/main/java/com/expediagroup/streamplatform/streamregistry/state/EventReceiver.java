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
package com.expediagroup.streamplatform.streamregistry.state;

import java.io.Closeable;

import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;

/**
 * An interface for receiving an ordered stream of {@link Event Events}
 */
public interface EventReceiver extends Closeable {
  /**
   * Commences receiving events and invokes the provided {@link EventReceiverListener} for each event. When the
   * receiver is fully loaded it passes the {@link Event#LOAD_COMPLETE} event then continues passes events
   * as they arrive.
   *
   * @param listener to be invoked for each event received.
   */
  void receive(EventReceiverListener listener);
}
