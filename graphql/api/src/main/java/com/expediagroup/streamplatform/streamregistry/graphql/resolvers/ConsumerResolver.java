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
package com.expediagroup.streamplatform.streamregistry.graphql.resolvers;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.ConsumerBindingService;
import com.expediagroup.streamplatform.streamregistry.core.services.StreamService;
import com.expediagroup.streamplatform.streamregistry.core.services.ZoneService;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.Zone;

@Component
@RequiredArgsConstructor
public class ConsumerResolver implements Resolvers.ConsumerResolver {
  private final StreamService streamService;
  private final ZoneService zoneService;
  private final ConsumerBindingService consumerBindingService;

  public Stream stream(Consumer consumer) {
    return streamService.read(consumer.getKey().getStreamKey()).orElse(null);
  }

  public Zone zone(Consumer consumer) {
    return zoneService.read(consumer.getKey().getZoneKey()).orElse(null);
  }

  public ConsumerBinding binding(Consumer consumer) {
    return consumerBindingService.find(consumer.getKey()).orElse(null);
  }
}
