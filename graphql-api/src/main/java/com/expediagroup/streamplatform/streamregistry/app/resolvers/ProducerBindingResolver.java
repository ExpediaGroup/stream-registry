package com.expediagroup.streamplatform.streamregistry.app.resolvers;
/**
 * Copyright (C) 2016-2019 Expedia Inc.
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

import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.expediagroup.streamplatform.streamregistry.app.Producer;
import com.expediagroup.streamplatform.streamregistry.app.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.app.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.app.keys.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.app.keys.StreamBindingKey;
import com.expediagroup.streamplatform.streamregistry.app.services.Services;

@Component
public class ProducerBindingResolver implements GraphQLResolver<ProducerBinding> {

  private Services services;

  public ProducerBindingResolver(Services services) {
    this.services = services;
  }

  public Producer producer(ProducerBinding producerBinding) {
    ProducerKey producerKey = new ProducerKey(
        producerBinding.getKey().getStreamDomain(),
        producerBinding.getKey().getStreamName(),
        producerBinding.getKey().getStreamVersion(),
        producerBinding.getKey().getInfrastructureZone(),
        producerBinding.getKey().getInfrastructureName()
    );

    return services.getProducerService().read(producerKey).orElse(null);
  }

  public StreamBinding binding(ProducerBinding producerBinding) {
    StreamBindingKey streamBindingKey = new StreamBindingKey(
        producerBinding.getKey().getStreamDomain(),
        producerBinding.getKey().getStreamName(),
        producerBinding.getKey().getStreamVersion(),
        producerBinding.getKey().getInfrastructureZone(),
        producerBinding.getKey().getInfrastructureName()
    );
    return services.getStreamBindingService().read(streamBindingKey).orElse(null);
  }
}