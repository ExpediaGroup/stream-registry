/**
 * Copyright (C) 2018-2019 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.app;

import java.util.Optional;

import com.expediagroup.streamplatform.streamregistry.model.Stated;

public class StateHelper {

  public static void maintainState(Stated stated, Optional<? extends Stated> existing) {
    if (existing.isPresent()) {
      stated.setStatus(existing.get().getStatus());
    }
  }
}
