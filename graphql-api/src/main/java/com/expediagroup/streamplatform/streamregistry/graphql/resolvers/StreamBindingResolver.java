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
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.InfrastructureKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;

@Component
public class StreamBindingResolver implements GraphQLResolver<StreamBinding> {

  private Services services;

  public StreamBindingResolver(Services services) {
    this.services = services;
  }

  public Stream stream(StreamBinding streamBinding) {
    StreamKey streamKey = new StreamKey(
        streamBinding.getKey().getStreamDomain(),
        streamBinding.getKey().getStreamName(),
        streamBinding.getKey().getStreamVersion()
    );
    return services.getStreamService().read(streamKey).orElse(null);
  }

  public Infrastructure infrastructure(StreamBinding streamBinding) {
    InfrastructureKey infrastructureKey = new InfrastructureKey(
        streamBinding.getKey().getInfrastructureZone(),
        streamBinding.getKey().getInfrastructureName()
    );

    return services.getInfrastructureService().read(infrastructureKey).orElse(null);
  }
}