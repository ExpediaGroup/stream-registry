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
package com.expediagroup.streamplatform.streamregistry;

import static com.expediagroup.streamplatform.streamregistry.core.handlers.IdentityHandler.DEFAULT;

import java.time.Clock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.expediagroup.streamplatform.streamregistry.core.handlers.IdentityHandler;
import com.expediagroup.streamplatform.streamregistry.handler.Handler;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.Zone;

@Configuration
@ComponentScan
public class StreamRegistryAutoConfiguration {
  @ConditionalOnMissingBean(value = Domain.class, parameterizedContainer = Handler.class)
  @Bean
  Handler<Domain> defaultDomainHandler() {
    return new IdentityHandler<>(DEFAULT, Domain.class);
  }

  @ConditionalOnMissingBean(value = Schema.class, parameterizedContainer = Handler.class)
  @Bean
  Handler<Schema> defaultSchemaHandler() {
    return new IdentityHandler<>(DEFAULT, Schema.class);
  }

  @ConditionalOnMissingBean(value = Stream.class, parameterizedContainer = Handler.class)
  @Bean
  Handler<Stream> defaultStreamHandler() {
    return new IdentityHandler<>(DEFAULT, Stream.class);
  }

  @ConditionalOnMissingBean(value = Zone.class, parameterizedContainer = Handler.class)
  @Bean
  Handler<Zone> defaultZoneHandler() {
    return new IdentityHandler<>(DEFAULT, Zone.class);
  }

  @ConditionalOnMissingBean(value = Infrastructure.class, parameterizedContainer = Handler.class)
  @Bean
  Handler<Infrastructure> defaultInfrastructureHandler() {
    return new IdentityHandler<>(DEFAULT, Infrastructure.class);
  }

  @ConditionalOnMissingBean(value = Producer.class, parameterizedContainer = Handler.class)
  @Bean
  Handler<Producer> defaultProducerHandler() {
    return new IdentityHandler<>(DEFAULT, Producer.class);
  }

  @ConditionalOnMissingBean(value = Consumer.class, parameterizedContainer = Handler.class)
  @Bean
  Handler<Consumer> defaultConsumerHandler() {
    return new IdentityHandler<>(DEFAULT, Consumer.class);
  }

  @ConditionalOnMissingBean(value = StreamBinding.class, parameterizedContainer = Handler.class)
  @Bean
  Handler<StreamBinding> defaultStreamBindingHandler() {
    return new IdentityHandler<>(DEFAULT, StreamBinding.class);
  }

  @ConditionalOnMissingBean(value = ProducerBinding.class, parameterizedContainer = Handler.class)
  @Bean
  Handler<ProducerBinding> defaultProducerBindingHandler() {
    return new IdentityHandler<>(DEFAULT, ProducerBinding.class);
  }

  @ConditionalOnMissingBean(value = ConsumerBinding.class, parameterizedContainer = Handler.class)
  @Bean
  Handler<ConsumerBinding> defaultConsumerBindingHandler() {
    return new IdentityHandler<>(DEFAULT, ConsumerBinding.class);
  }

  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }
}
