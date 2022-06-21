/**
 * Copyright (C) 2018-2022 Expedia, Inc.
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

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.expediagroup.streamplatform.streamregistry.core.validators.key.ConsumerBindingKeyValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.key.ConsumerKeyValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.key.DomainKeyValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.key.InfrastructureKeyValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.key.KeyValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.key.ProcessBindingKeyValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.key.ProcessKeyValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.key.ProducerBindingKeyValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.key.ProducerKeyValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.key.SchemaKeyValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.key.StreamBindingKeyValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.key.StreamKeyValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.key.ZoneKeyValidator;
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

@Configuration
public class StreamRegistryAutoConfiguration {

  @ConditionalOnMissingBean(value = ConsumerBinding.class, parameterizedContainer = KeyValidator.class)
  @Bean
  public KeyValidator<ConsumerBinding> consumerBindingKeyValidator() {
    return new ConsumerBindingKeyValidator();
  }

  @ConditionalOnMissingBean(value = Consumer.class, parameterizedContainer = KeyValidator.class)
  @Bean
  public KeyValidator<Consumer> consumerKeyValidator() {
    return new ConsumerKeyValidator();
  }

  @ConditionalOnMissingBean(value = Domain.class, parameterizedContainer = KeyValidator.class)
  @Bean
  public KeyValidator<Domain> domainKeyValidator() {
    return new DomainKeyValidator();
  }

  @ConditionalOnMissingBean(value = Infrastructure.class, parameterizedContainer = KeyValidator.class)
  @Bean
  public KeyValidator<Infrastructure> infrastructureKeyValidator() {
    return new InfrastructureKeyValidator();
  }

  @ConditionalOnMissingBean(value = ProcessBinding.class, parameterizedContainer = KeyValidator.class)
  @Bean
  public KeyValidator<ProcessBinding> processBindingKeyValidator() {
    return new ProcessBindingKeyValidator();
  }

  @ConditionalOnMissingBean(value = Process.class, parameterizedContainer = KeyValidator.class)
  @Bean
  public KeyValidator<Process> processKeyValidator() {
    return new ProcessKeyValidator();
  }

  @ConditionalOnMissingBean(value = ProducerBinding.class, parameterizedContainer = KeyValidator.class)
  @Bean
  public KeyValidator<ProducerBinding> producerBindingKeyValidator() {
    return new ProducerBindingKeyValidator();
  }

  @ConditionalOnMissingBean(value = Producer.class, parameterizedContainer = KeyValidator.class)
  @Bean
  public KeyValidator<Producer> producerKeyValidator() {
    return new ProducerKeyValidator();
  }

  @ConditionalOnMissingBean(value = Schema.class, parameterizedContainer = KeyValidator.class)
  @Bean
  public KeyValidator<Schema> schemaKeyValidator() {
    return new SchemaKeyValidator();
  }

  @ConditionalOnMissingBean(value = StreamBinding.class, parameterizedContainer = KeyValidator.class)
  @Bean
  public KeyValidator<StreamBinding> streamBindingKeyValidator() {
    return new StreamBindingKeyValidator();
  }

  @ConditionalOnMissingBean(value = Stream.class, parameterizedContainer = KeyValidator.class)
  @Bean
  public KeyValidator<Stream> streamKeyValidator() {
    return new StreamKeyValidator();
  }

  @ConditionalOnMissingBean(value = Zone.class, parameterizedContainer = KeyValidator.class)
  @Bean
  public KeyValidator<Zone> zoneKeyValidator() {
    return new ZoneKeyValidator();
  }
}
