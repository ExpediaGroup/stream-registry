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

import lombok.RequiredArgsConstructor;

import com.coxautodev.graphql.tools.GraphQLResolver;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.ConsumerService;
import com.expediagroup.streamplatform.streamregistry.core.services.StreamBindingService;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamBindingKey;

@Component
@RequiredArgsConstructor
public class ConsumerBindingResolver implements GraphQLResolver<ConsumerBinding> {
  private final ConsumerService consumerService;
  private final StreamBindingService streamBindingService;

  public Consumer consumer(ConsumerBinding consumerBinding) {

    ConsumerKey consumerKey = new ConsumerKey(
        consumerBinding.getKey().getStreamDomain(),
        consumerBinding.getKey().getStreamName(),
        consumerBinding.getKey().getStreamVersion(),
        consumerBinding.getKey().getInfrastructureZone(),
        consumerBinding.getKey().getConsumerName()
    );

    return consumerService.read(consumerKey).orElse(null);
  }

  public StreamBinding binding(ConsumerBinding consumerBinding) {
    StreamBindingKey streamBindingKey = new StreamBindingKey(
        consumerBinding.getKey().getStreamDomain(),
        consumerBinding.getKey().getStreamName(),
        consumerBinding.getKey().getStreamVersion(),
        consumerBinding.getKey().getInfrastructureZone(),
        consumerBinding.getKey().getInfrastructureName()
    );
    return streamBindingService.read(streamBindingKey).orElse(null);
  }
}
