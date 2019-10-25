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
package com.expediagroup.streamplatform.streamregistry.graphql.query;

import lombok.RequiredArgsConstructor;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Query implements GraphQLQueryResolver {
  private final DomainQuery domainQuery;
  private final SchemaQuery schemaQuery;
  private final StreamQuery streamQuery;
  private final ZoneQuery zoneQuery;
  private final InfrastructureQuery infrastructureQuery;
  private final ProducerQuery producerQuery;
  private final ConsumerQuery consumerQuery;
  private final StreamBindingQuery streamBindingQuery;
  private final ProducerBindingQuery producerBindingQuery;
  private final ConsumerBindingQuery consumerBindingQuery;

  public DomainQuery getDomain() {
    return domainQuery;
  }

  public SchemaQuery getSchema() {
    return schemaQuery;
  }

  public StreamQuery getStream() {
    return streamQuery;
  }

  public ZoneQuery getZone() {
    return zoneQuery;
  }

  public InfrastructureQuery getInfrastructure() {
    return infrastructureQuery;
  }

  public ProducerQuery getProducer() {
    return producerQuery;
  }

  public ConsumerQuery getConsumer() {
    return consumerQuery;
  }

  public StreamBindingQuery getStreamBinding() {
    return streamBindingQuery;
  }

  public ProducerBindingQuery getProducerBinding() {
    return producerBindingQuery;
  }

  public ConsumerBindingQuery getConsumerBinding() {
    return consumerBindingQuery;
  }
}