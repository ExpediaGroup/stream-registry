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
package com.expediagroup.streamplatform.streamregistry.repository;

import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfiguration {
  @Bean
  SupplierFactoryBean<ManagedKafkaProducer> producer(
      @Value("${streams.bootstrapServers}") String bootstrapServers,
      @Value("${streams.topic}") String topic,
      @Value("${schema.registry.url}") String schemaRegistryUrl,
      ManagedKafkaProducer.Factory factory) {
    return new SupplierFactoryBean<>(() -> factory.create(
        bootstrapServers,
        topic,
        schemaRegistryUrl),
        ManagedKafkaProducer.class);
  }

  @Bean
  SupplierFactoryBean<ManagedKStreams> view(
      @Value("${streams.bootstrapServers}") String bootstrapServers,
      @Value("${streams.topic}") String topic,
      ManagedKStreams.Factory factory) {
    return new SupplierFactoryBean<>(() -> factory.create(
        bootstrapServers,
        topic),
        ManagedKStreams.class);
  }

  @RequiredArgsConstructor
  static class SupplierFactoryBean<T> extends AbstractFactoryBean<T> {
    private final Supplier<T> supplier;
    private final Class<T> tClass;


    @Override
    protected T createInstance() {
      return supplier.get();
    }

    @Override
    public Class<?> getObjectType() {
      return tClass;
    }
  }
}
