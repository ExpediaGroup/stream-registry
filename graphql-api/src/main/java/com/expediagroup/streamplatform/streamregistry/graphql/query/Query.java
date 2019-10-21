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

import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlersForServices;
import com.expediagroup.streamplatform.streamregistry.core.services.Services;
import com.expediagroup.streamplatform.streamregistry.graphql.filters.ConsumerBindingFilter;
import com.expediagroup.streamplatform.streamregistry.graphql.filters.ConsumerFilter;
import com.expediagroup.streamplatform.streamregistry.graphql.filters.DomainFilter;
import com.expediagroup.streamplatform.streamregistry.graphql.filters.InfrastructureFilter;
import com.expediagroup.streamplatform.streamregistry.graphql.filters.ProducerBindingFilter;
import com.expediagroup.streamplatform.streamregistry.graphql.filters.ProducerFilter;
import com.expediagroup.streamplatform.streamregistry.graphql.filters.SchemaFilter;
import com.expediagroup.streamplatform.streamregistry.graphql.filters.StreamBindingFilter;
import com.expediagroup.streamplatform.streamregistry.graphql.filters.StreamFilter;
import com.expediagroup.streamplatform.streamregistry.graphql.filters.ZoneFilter;
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

  private final Services services;
  private final HandlersForServices handlersForServices;
  public Query(Services services, HandlersForServices handlersForServices) {
    this.services = services;
    this.handlersForServices = handlersForServices;
  }

  // TODO to work around a graphql bug, remove when fixed
  public Domain bugfixq(TagQuery v) {
    return null;
  }

  public Domain bugfixi(TagInput v) {
    return null;
  }

  public Domain getDomain(DomainKeyInput key) {
    return services.getDomainService().read(key.asDomainKey()).get();
  }

  public Iterable<Domain> getDomains(DomainKeyQuery key, SpecificationQuery specification) {
    return services.getDomainService().findAll(new DomainFilter(key, specification));
  }

  public Schema getSchema(SchemaKeyInput key) {
    return services.getSchemaService().read(key.asSchemaKey()).get();
  }

  public Iterable<Schema> getSchemas(SchemaKeyQuery key, SpecificationQuery specification) {
    return services.getSchemaService().findAll(new SchemaFilter(key, specification));
  }

  public Stream getStream(StreamKeyInput key) {
    return services.getStreamService().read(key.asStreamKey()).get();
  }

  public Iterable<Stream> getStreams(StreamKeyQuery key, SpecificationQuery specification, SchemaKeyQuery schema) {
    return services.getStreamService().findAll(new StreamFilter(key, specification, schema));
  }

  public Zone getZone(ZoneKeyInput key) {
    return services.getZoneService().read(key.asZoneKey()).get();
  }

  public Iterable<Zone> getZones(ZoneKeyQuery key, SpecificationQuery specification) {
    return services.getZoneService().findAll(new ZoneFilter(key, specification));
  }

  public Infrastructure getInfrastructure(InfrastructureKeyInput key) {
    return services.getInfrastructureService().read(key.asInfrastructureKey()).get();
  }

  public Iterable<Infrastructure> getInfrastructures(InfrastructureKeyQuery key, SpecificationQuery specification) {
    return services.getInfrastructureService().findAll(new InfrastructureFilter(key, specification));
  }

  public Producer getProducer(ProducerKeyInput key) {
    return services.getProducerService().read(key.asProducerKey()).get();
  }

  public Iterable<Producer> getProducers(ProducerKeyQuery key, SpecificationQuery specification) {
    return services.getProducerService().findAll(new ProducerFilter(key, specification));
  }

  public Consumer getConsumer(ConsumerKeyInput key) {
    return services.getConsumerService().read(key.asConsumerKey()).get();
  }

  public Iterable<Consumer> getConsumers(ConsumerKeyQuery key, SpecificationQuery specification) {
    return services.getConsumerService().findAll(new ConsumerFilter(key, specification));
  }

  public StreamBinding getStreamBinding(StreamBindingKeyInput key) {
    return services.getStreamBindingService().read(key.asStreamBindingKey()).get();
  }

  public Iterable<StreamBinding> getStreamBindings(StreamBindingKeyQuery key, SpecificationQuery specification) {
    return services.getStreamBindingService().findAll(new StreamBindingFilter(key, specification));
  }

  public ProducerBinding getProducerBinding(ProducerBindingKeyInput key) {
    return services.getProducerBindingService().read(key.asProducerBindingKey()).get();
  }

  public Iterable<ProducerBinding> getProducerBindings(ProducerBindingKeyQuery key, SpecificationQuery specification) {
    return services.getProducerBindingService().findAll(new ProducerBindingFilter(key, specification));
  }

  public ConsumerBinding getConsumerBinding(ConsumerBindingKeyInput key) {
    return services.getConsumerBindingService().read(key.asConsumerBindingKey()).get();
  }

  public Iterable<ConsumerBinding> getConsumerBindings(ConsumerBindingKeyQuery key, SpecificationQuery specification) {
    return services.getConsumerBindingService().findAll(new ConsumerBindingFilter(key, specification));
  }
}