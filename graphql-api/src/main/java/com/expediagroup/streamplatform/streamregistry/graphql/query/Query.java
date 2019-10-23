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

import com.coxautodev.graphql.tools.GraphQLQueryResolver;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.Services;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.TagInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.TagQuery;
import com.expediagroup.streamplatform.streamregistry.model.Domain;

@Component
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

  public Query(Services services) {
    this.domainQuery = new DomainQuery(services.getDomainService());
    this.schemaQuery = new SchemaQuery(services.getSchemaService());
    this.streamQuery = new StreamQuery(services.getStreamService());
    this.zoneQuery = new ZoneQuery(services.getZoneService());
    this.infrastructureQuery = new InfrastructureQuery(services.getInfrastructureService());
    this.producerQuery = new ProducerQuery(services.getProducerService());
    this.consumerQuery = new ConsumerQuery(services.getConsumerService());
    this.streamBindingQuery = new StreamBindingQuery(services.getStreamBindingService());
    this.producerBindingQuery = new ProducerBindingQuery(services.getProducerBindingService());
    this.consumerBindingQuery = new ConsumerBindingQuery(services.getConsumerBindingService());
  }

  public Domain bugfixq(TagQuery v) {
    return null;
  }

  public Domain bugfixi(TagInput v) {
    return null;
  }

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