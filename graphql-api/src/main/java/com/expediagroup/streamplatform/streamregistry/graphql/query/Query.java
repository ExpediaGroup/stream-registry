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
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ConsumerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ConsumerKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.DomainKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.InfrastructureKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ProducerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ProducerKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SchemaKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StreamBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StreamKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.TagInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ZoneKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.ConsumerBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.ConsumerKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.DomainKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.InfrastructureKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.ProducerBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.ProducerKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SchemaKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.StreamBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.StreamKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.TagQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.ZoneKeyQuery;
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

  // TODO to work around a graphql bug, remove when fixed
  public Domain bugfixq(TagQuery v) {
    return null;
  }

  public Domain bugfixi(TagInput v) {
    return null;
  }

  @Deprecated
  public Domain getDomain(DomainKeyInput key) {
    return domainQuery.byKey(key);
  }

  @Deprecated
  public Iterable<Domain> getDomains(DomainKeyQuery key, SpecificationQuery specification) {
    return domainQuery.byQuery(key, specification);
  }

  @Deprecated
  public Schema getSchema(SchemaKeyInput key) {
    return schemaQuery.getSchema(key);
  }

  @Deprecated
  public Iterable<Schema> getSchemas(SchemaKeyQuery key, SpecificationQuery specification) {
    return schemaQuery.getSchemas(key, specification);
  }

  @Deprecated
  public Stream getStream(StreamKeyInput key) {
    return streamQuery.getStream(key);
  }

  @Deprecated
  public Iterable<Stream> getStreams(StreamKeyQuery key, SpecificationQuery specification, SchemaKeyQuery schema) {
    return streamQuery.getStreams(key, specification, schema);
  }

  @Deprecated
  public Zone getZone(ZoneKeyInput key) {
    return zoneQuery.getZone(key);
  }

  @Deprecated
  public Iterable<Zone> getZones(ZoneKeyQuery key, SpecificationQuery specification) {
    return zoneQuery.getZones(key, specification);
  }

  @Deprecated
  public Infrastructure getInfrastructure(InfrastructureKeyInput key) {
    return infrastructureQuery.getInfrastructure(key);
  }

  @Deprecated
  public Iterable<Infrastructure> getInfrastructures(InfrastructureKeyQuery key, SpecificationQuery specification) {
    return infrastructureQuery.getInfrastructures(key, specification);
  }

  @Deprecated
  public Producer getProducer(ProducerKeyInput key) {
    return producerQuery.getProducer(key);
  }

  @Deprecated
  public Iterable<Producer> getProducers(ProducerKeyQuery key, SpecificationQuery specification) {
    return producerQuery.getProducers(key, specification);
  }

  @Deprecated
  public Consumer getConsumer(ConsumerKeyInput key) {
    return consumerQuery.getConsumer(key);
  }

  @Deprecated
  public Iterable<Consumer> getConsumers(ConsumerKeyQuery key, SpecificationQuery specification) {
    return consumerQuery.getConsumers(key, specification);
  }

  @Deprecated
  public StreamBinding getStreamBinding(StreamBindingKeyInput key) {
    return streamBindingQuery.getStreamBinding(key);
  }

  @Deprecated
  public Iterable<StreamBinding> getStreamBindings(StreamBindingKeyQuery key, SpecificationQuery specification) {
    return streamBindingQuery.getStreamBindings(key, specification);
  }

  @Deprecated
  public ProducerBinding getProducerBinding(ProducerBindingKeyInput key) {
    return producerBindingQuery.getProducerBinding(key);
  }

  @Deprecated
  public Iterable<ProducerBinding> getProducerBindings(ProducerBindingKeyQuery key, SpecificationQuery specification) {
    return producerBindingQuery.getProducerBindings(key, specification);
  }

  @Deprecated
  public ConsumerBinding getConsumerBinding(ConsumerBindingKeyInput key) {
    return consumerBindingQuery.getConsumerBinding(key);
  }

  @Deprecated
  public Iterable<ConsumerBinding> getConsumerBindings(ConsumerBindingKeyQuery key, SpecificationQuery specification) {
    return consumerBindingQuery.getConsumerBindings(key, specification);
  }

  public DomainQuery getDomainQuery() {
    return domainQuery;
  }

  public SchemaQuery getSchemaQuery() {
    return schemaQuery;
  }

  public StreamQuery getStreamQuery() {
    return streamQuery;
  }

  public ZoneQuery getZoneQuery() {
    return zoneQuery;
  }

  public InfrastructureQuery getInfrastructureQuery() {
    return infrastructureQuery;
  }

  public ProducerQuery getProducerQuery() {
    return producerQuery;
  }

  public ConsumerQuery getConsumerQuery() {
    return consumerQuery;
  }

  public StreamBindingQuery getStreamBindingQuery() {
    return streamBindingQuery;
  }

  public ProducerBindingQuery getProducerBindingQuery() {
    return producerBindingQuery;
  }

  public ConsumerBindingQuery getConsumerBindingQuery() {
    return consumerBindingQuery;
  }
}