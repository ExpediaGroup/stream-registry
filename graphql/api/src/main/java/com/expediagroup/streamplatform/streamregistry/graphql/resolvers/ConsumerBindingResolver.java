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

import com.expediagroup.streamplatform.streamregistry.core.services.ConsumerService;
import com.expediagroup.streamplatform.streamregistry.core.services.StreamBindingService;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConsumerBindingResolver implements Resolvers.ConsumerBindingResolver {
  private final ConsumerService consumerService;
  private final StreamBindingService streamBindingService;

  public Consumer consumer(ConsumerBinding consumerBinding) {
    return consumerService.get(consumerBinding.getKey().getConsumerKey()).orElse(null);
  }

  public StreamBinding binding(ConsumerBinding consumerBinding) {
    return streamBindingService.get(consumerBinding.getKey().getStreamBindingKey()).orElse(null);
  }
}
