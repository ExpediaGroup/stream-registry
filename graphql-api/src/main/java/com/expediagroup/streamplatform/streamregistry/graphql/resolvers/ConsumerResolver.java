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
package com.expediagroup.streamplatform.streamregistry.graphql.resolvers;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import lombok.RequiredArgsConstructor;

import com.coxautodev.graphql.tools.GraphQLResolver;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.ConsumerBindingService;
import com.expediagroup.streamplatform.streamregistry.core.services.StreamService;
import com.expediagroup.streamplatform.streamregistry.core.services.ZoneService;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.Zone;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;

@Component
@RequiredArgsConstructor
public class ConsumerResolver implements GraphQLResolver<Consumer> {
  private final StreamService streamService;
  private final ZoneService zoneService;
  private final ConsumerBindingService consumerBindingService;

  public Stream stream(Consumer consumer) {
    StreamKey streamKey = new StreamKey(
        consumer.getKey().getStreamDomain(),
        consumer.getKey().getStreamName(),
        consumer.getKey().getStreamVersion()
    );
    return streamService.read(streamKey).orElse(null);
  }

  public Zone zone(Consumer consumer) {
    ZoneKey zoneKey = new ZoneKey(
        consumer.getKey().getZone()
    );
    return zoneService.read(zoneKey).orElse(null);
  }

  public Optional<ConsumerBinding> binding(Consumer consumer) {
    Predicate<ConsumerBinding> predicate = binding -> binding.getKey().getConsumerKey().equals(consumer.getKey());
    return StreamSupport
        .stream(consumerBindingService.findAll(predicate).spliterator(), false)
        .findFirst();
  }
}
