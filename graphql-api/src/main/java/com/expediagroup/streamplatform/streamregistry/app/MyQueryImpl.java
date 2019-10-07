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
package com.expediagroup.streamplatform.streamregistry.app;

import static com.expediagroup.streamplatform.streamregistry.app.convertors.ZoneKeyInputConvertor.convert;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.app.inputs.ConsumerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.ConsumerKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.DomainKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.InfrastructureKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.ProducerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.ProducerKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.SchemaKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.StreamBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.StreamKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.ZoneKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.queries.ConsumerBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.app.queries.ConsumerKeyQuery;
import com.expediagroup.streamplatform.streamregistry.app.queries.DomainKeyQuery;
import com.expediagroup.streamplatform.streamregistry.app.queries.InfrastructureKeyQuery;
import com.expediagroup.streamplatform.streamregistry.app.queries.ProducerBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.app.queries.ProducerKeyQuery;
import com.expediagroup.streamplatform.streamregistry.app.queries.Query;
import com.expediagroup.streamplatform.streamregistry.app.queries.SchemaKeyQuery;
import com.expediagroup.streamplatform.streamregistry.app.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.app.queries.StreamBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.app.queries.StreamKeyQuery;
import com.expediagroup.streamplatform.streamregistry.app.queries.ZoneKeyQuery;
import com.expediagroup.streamplatform.streamregistry.app.queryFilters.ConsumerBindingFilter;
import com.expediagroup.streamplatform.streamregistry.app.queryFilters.ConsumerFilter;
import com.expediagroup.streamplatform.streamregistry.app.queryFilters.DomainFilter;
import com.expediagroup.streamplatform.streamregistry.app.queryFilters.InfrastructureFilter;
import com.expediagroup.streamplatform.streamregistry.app.queryFilters.ProducerBindingFilter;
import com.expediagroup.streamplatform.streamregistry.app.queryFilters.ProducerFilter;
import com.expediagroup.streamplatform.streamregistry.app.queryFilters.SchemaFilter;
import com.expediagroup.streamplatform.streamregistry.app.queryFilters.StreamBindingFilter;
import com.expediagroup.streamplatform.streamregistry.app.queryFilters.StreamFilter;
import com.expediagroup.streamplatform.streamregistry.app.queryFilters.ZoneFilter;
import com.expediagroup.streamplatform.streamregistry.core.handler.HandlersForServices;
import com.expediagroup.streamplatform.streamregistry.core.services.Services;
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
public class MyQueryImpl implements Query {

  private Services services;
  private HandlersForServices handlersForServices;

  public MyQueryImpl(Services services, HandlersForServices handlersForServices) {
    this.services = services;
    this.handlersForServices = handlersForServices;
  }

  @Override
  public Domain getDomain(DomainKeyInput key) {
    return services.getDomainService().read(key.asDomainKey()).get();
  }

  @Override
  public Iterable<Domain> getDomains(DomainKeyQuery key, SpecificationQuery specification) {
    return services.getDomainService().findAll(new DomainFilter(key, specification));
  }

  @Override
  public Schema getSchema(SchemaKeyInput key) {
    return services.getSchemaService().read(key.asSchemaKey()).get();
  }

  @Override
  public Iterable<Schema> getSchemas(SchemaKeyQuery key, SpecificationQuery specification) {
    return services.getSchemaService().findAll(new SchemaFilter(key, specification));
  }

  @Override
  public Stream getStream(StreamKeyInput key) {
    return services.getStreamService().read(key.asStreamKey()).get();
  }

  @Override
  public Iterable<Stream> getStreams(StreamKeyQuery key, SpecificationQuery specification, SchemaKeyQuery schema) {
    return services.getStreamService().findAll(new StreamFilter(key, specification, schema));
  }

  @Override
  public Zone getZone(ZoneKeyInput key) {
    return services.getZoneService().read(convert(key)).get();
  }

  @Override
  public Iterable<Zone> getZones(ZoneKeyQuery key, SpecificationQuery specification) {
    return services.getZoneService().findAll(new ZoneFilter(key, specification));
  }

  @Override
  public Infrastructure getInfrastructure(InfrastructureKeyInput key) {
    return services.getInfrastructureService().read(key.asInfrastructureKey()).get();
  }

  @Override
  public Iterable<Infrastructure> getInfrastructures(InfrastructureKeyQuery key, SpecificationQuery specification) {
    return services.getInfrastructureService().findAll(new InfrastructureFilter(key, specification));
  }

  @Override
  public Producer getProducer(ProducerKeyInput key) {
    return services.getProducerService().read(key.asProducerKey()).get();
  }

  @Override
  public Iterable<Producer> getProducers(ProducerKeyQuery key, SpecificationQuery specification) {
    return services.getProducerService().findAll(new ProducerFilter(key, specification));
  }

  @Override
  public Consumer getConsumer(ConsumerKeyInput key) {
    return services.getConsumerService().read(key.asConsumerKey()).get();
  }

  @Override
  public Iterable<Consumer> getConsumers(ConsumerKeyQuery key, SpecificationQuery specification) {
    return services.getConsumerService().findAll(new ConsumerFilter(key, specification));
  }

  @Override
  public StreamBinding getStreamBinding(StreamBindingKeyInput key) {
    return services.getStreamBindingService().read(key.asStreamBindingKey()).get();
  }

  @Override
  public Iterable<StreamBinding> getStreamBindings(StreamBindingKeyQuery key, SpecificationQuery specification) {
    return services.getStreamBindingService().findAll(new StreamBindingFilter(key, specification));
  }

  @Override
  public ProducerBinding getProducerBinding(ProducerBindingKeyInput key) {
    return services.getProducerBindingService().read(key.asProducerBindingKey()).get();
  }

  @Override
  public Iterable<ProducerBinding> getProducerBindings(ProducerBindingKeyQuery key, SpecificationQuery specification) {
    return services.getProducerBindingService().findAll(new ProducerBindingFilter(key, specification));
  }

  @Override
  public ConsumerBinding getConsumerBinding(ConsumerBindingKeyInput key) {
    return services.getConsumerBindingService().read(key.asConsumerBindingKey()).get();
  }

  @Override
  public Iterable<ConsumerBinding> getConsumerBindings(ConsumerBindingKeyQuery key, SpecificationQuery specification) {
    return services.getConsumerBindingService().findAll(new ConsumerBindingFilter(key, specification));
  }

  @Override
  public Iterable<String> types(){
    return handlersForServices.getTypes();
  }
}
