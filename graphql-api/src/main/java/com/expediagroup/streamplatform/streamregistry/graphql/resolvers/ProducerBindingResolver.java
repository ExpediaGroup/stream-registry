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

import com.coxautodev.graphql.tools.GraphQLResolver;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.ProducerService;
import com.expediagroup.streamplatform.streamregistry.core.services.StreamBindingService;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamBindingKey;

@Component
@RequiredArgsConstructor
public class ProducerBindingResolver implements GraphQLResolver<ProducerBinding> {
  private final ProducerService producerService;
  private final StreamBindingService streamBindingService;

  public Producer producer(ProducerBinding producerBinding) {
    ProducerKey producerKey = new ProducerKey(
        producerBinding.getKey().getStreamDomain(),
        producerBinding.getKey().getStreamName(),
        producerBinding.getKey().getStreamVersion(),
        producerBinding.getKey().getInfrastructureZone(),
        producerBinding.getKey().getProducerName()
    );

    return producerService.read(producerKey).orElse(null);
  }

  public StreamBinding binding(ProducerBinding producerBinding) {
    StreamBindingKey streamBindingKey = new StreamBindingKey(
        producerBinding.getKey().getStreamDomain(),
        producerBinding.getKey().getStreamName(),
        producerBinding.getKey().getStreamVersion(),
        producerBinding.getKey().getInfrastructureZone(),
        producerBinding.getKey().getInfrastructureName()
    );
    return streamBindingService.read(streamBindingKey).orElse(null);
  }
}
