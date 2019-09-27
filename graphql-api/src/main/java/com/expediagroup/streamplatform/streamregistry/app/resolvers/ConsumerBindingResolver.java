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
import com.expediagroup.streamplatform.streamregistry.app.Consumer;
import com.expediagroup.streamplatform.streamregistry.app.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.app.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.app.keys.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.app.keys.StreamBindingKey;
import com.expediagroup.streamplatform.streamregistry.app.services.Services;

@Component
public class ConsumerBindingResolver implements GraphQLResolver<ConsumerBinding> {

  private Services services;

  public ConsumerBindingResolver(Services services) {
    this.services = services;
  }

  public Consumer consumer(ConsumerBinding consumerBinding) {

    ConsumerKey consumerKey = new ConsumerKey(
        consumerBinding.getKey().getStreamDomain(),
        consumerBinding.getKey().getStreamName(),
        consumerBinding.getKey().getStreamVersion(),
        consumerBinding.getKey().getInfrastructureZone(),
        consumerBinding.getKey().getInfrastructureName()
    );

    return services.getConsumerService().read(consumerKey).orElse(null);
  }

  public StreamBinding binding(ConsumerBinding consumerBinding) {
    StreamBindingKey streamBindingKey = new StreamBindingKey(
        consumerBinding.getKey().getStreamDomain(),
        consumerBinding.getKey().getStreamName(),
        consumerBinding.getKey().getStreamVersion(),
        consumerBinding.getKey().getInfrastructureZone(),
        consumerBinding.getKey().getInfrastructureName()
    );
    return services.getStreamBindingService().read(streamBindingKey).orElse(null);
  }
}