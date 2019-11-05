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
package com.expediagroup.streamplatform.streamregistry.handler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.Zone;

@Configuration
public class EgspHandlerConfiguration {
  @Bean
  Handler<Domain> domainHandler() {
    return new IdentityHandler<>("default", Domain.class);
  }

  @Bean
  Handler<Zone> zoneHandler() {
    return new IdentityHandler<>("egsp.kafka", Zone.class);
  }

  @Bean
  Handler<Infrastructure> infrastructureHandler() {
    return new IdentityHandler<>("egsp.kafka", Infrastructure.class);
  }

  @Bean
  Handler<Producer> producerHandler() {
    return new IdentityHandler<>("egsp.kafka", Producer.class);
  }

  @Bean
  Handler<Consumer> consumerHandler() {
    return new IdentityHandler<>("egsp.kafka", Consumer.class);
  }

  @Bean
  Handler<StreamBinding> streamBindingHandler() {
    return new IdentityHandler<>("egsp.kafka", StreamBinding.class);
  }

  @Bean
  Handler<ProducerBinding> producerBindingHandler() {
    return new IdentityHandler<>("egsp.kafka", ProducerBinding.class);
  }

  @Bean
  Handler<ConsumerBinding> consumerBindingHandler() {
    return new IdentityHandler<>("egsp.kafka", ConsumerBinding.class);
  }
}
