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

import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.expediagroup.streamplatform.streamregistry.core.services.Services;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.Zone;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;

@Component
public class ProducerResolver implements GraphQLResolver<Producer> {

  private Services services;

  public ProducerResolver(Services services) {
    this.services = services;
  }

  public Stream stream(Producer producer) {
    StreamKey streamKey = new StreamKey(
        producer.getKey().getStreamDomain(),
        producer.getKey().getStreamName(),
        producer.getKey().getStreamVersion()
    );
    return services.getStreamService().read(streamKey).get();
  }

  public Zone zone(Producer producer) {
    ZoneKey zoneKey = new ZoneKey(
        producer.getKey().getZone()
    );
    return services.getZoneService().read(zoneKey).orElse(null);
  }
}