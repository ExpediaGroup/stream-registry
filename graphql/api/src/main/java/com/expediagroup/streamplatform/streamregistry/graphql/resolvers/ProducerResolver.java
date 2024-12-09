/**
 * Copyright (C) 2018-2024 Expedia, Inc.
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

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.ProducerBindingService;
import com.expediagroup.streamplatform.streamregistry.core.services.StreamService;
import com.expediagroup.streamplatform.streamregistry.core.services.ZoneService;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.Zone;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProducerResolver implements Resolvers.ProducerResolver {
  private final StreamService streamService;
  private final ZoneService zoneService;
  private final ProducerBindingService producerBindingService;

  public Stream stream(Producer producer) {
    return streamService.get(producer.getKey().getStreamKey()).get();
  }

  public Zone zone(Producer producer) {
    return zoneService.get(producer.getKey().getZoneKey()).orElse(null);
  }

  public ProducerBinding binding(Producer producer) {
    return producerBindingService.find(producer.getKey()).orElse(null);
  }
}
