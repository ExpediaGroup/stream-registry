/**
 * Copyright (C) 2018-2021 Expedia, Inc.
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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.expediagroup.streamplatform.streamregistry.core.handlers.IdentityHandler;
import com.expediagroup.streamplatform.streamregistry.handler.Handler;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.model.Process;
import com.expediagroup.streamplatform.streamregistry.model.ProcessBinding;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.Zone;

@SpringBootApplication
public class StreamRegistryApp {
  public static void main(String[] args) {
    SpringApplication.run(StreamRegistryApp.class, args);
  }

  @Bean
  Handler<Domain> defaultDomainHandler() {
    return new IdentityHandler<>(DEFAULT, Domain.class);
  }

  @Bean
  Handler<Schema> defaultSchemaHandler() {
    return new IdentityHandler<>(DEFAULT, Schema.class);
  }

  @Bean
  Handler<Stream> defaultStreamHandler() {
    return new IdentityHandler<>(DEFAULT, Stream.class);
  }

  @Bean
  Handler<Zone> defaultZoneHandler() {
    return new IdentityHandler<>(DEFAULT, Zone.class);
  }

  @Bean
  Handler<Infrastructure> defaultInfrastructureHandler() {
    return new IdentityHandler<>(DEFAULT, Infrastructure.class);
  }

  @Bean
  Handler<Producer> defaultProducerHandler() {
    return new IdentityHandler<>(DEFAULT, Producer.class);
  }

  @Bean
  Handler<Consumer> defaultConsumerHandler() {
    return new IdentityHandler<>(DEFAULT, Consumer.class);
  }

  @Bean
  Handler<Process> defaultProcessHandler() {
    return new IdentityHandler<>(DEFAULT, Process.class);
  }

  @Bean
  Handler<StreamBinding> defaultStreamBindingHandler() {
    return new IdentityHandler<>(DEFAULT, StreamBinding.class);
  }

  @Bean
  Handler<ProducerBinding> defaultProducerBindingHandler() {
    return new IdentityHandler<>(DEFAULT, ProducerBinding.class);
  }

  @Bean
  Handler<ConsumerBinding> defaultConsumerBindingHandler() {
    return new IdentityHandler<>(DEFAULT, ConsumerBinding.class);
  }

  @Bean
  Handler<ProcessBinding> defaultProcessBindingHandler() {
    return new IdentityHandler<>(DEFAULT, ProcessBinding.class);
  }
}
