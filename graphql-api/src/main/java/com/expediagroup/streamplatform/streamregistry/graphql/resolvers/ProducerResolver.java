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

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import lombok.RequiredArgsConstructor;

import com.coxautodev.graphql.tools.GraphQLResolver;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.ProducerBindingService;
import com.expediagroup.streamplatform.streamregistry.core.services.StreamService;
import com.expediagroup.streamplatform.streamregistry.core.services.ZoneService;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.Zone;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;

@Component
@RequiredArgsConstructor
public class ProducerResolver implements GraphQLResolver<Producer> {
  private final StreamService streamService;
  private final ZoneService zoneService;
  private final ProducerBindingService producerBindingService;

  public Stream stream(Producer producer) {
    StreamKey streamKey = new StreamKey(
        producer.getKey().getStreamDomain(),
        producer.getKey().getStreamName(),
        producer.getKey().getStreamVersion()
    );
    return streamService.read(streamKey).get();
  }

  public Zone zone(Producer producer) {
    ZoneKey zoneKey = new ZoneKey(
        producer.getKey().getZone()
    );
    return zoneService.read(zoneKey).orElse(null);
  }

  public Optional<ProducerBinding> binding(Producer producer) {
    Predicate<ProducerBinding> predicate = binding -> binding.getKey().getProducerKey().equals(producer.getKey());
    return StreamSupport
        .stream(producerBindingService.findAll(predicate).spliterator(), false)
        .findFirst();
  }

  public Status status(Producer producer) {
    return producer.getStatus() == null ? new Status() : producer.getStatus();
  }
}
