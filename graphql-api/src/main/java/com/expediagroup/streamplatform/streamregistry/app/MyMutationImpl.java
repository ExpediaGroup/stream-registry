/**
 * Copyright (C) 2016-2019 Expedia Inc.
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

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.app.Consumer;
import com.expediagroup.streamplatform.streamregistry.app.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.app.Domain;
import com.expediagroup.streamplatform.streamregistry.app.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.app.Producer;
import com.expediagroup.streamplatform.streamregistry.app.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.app.Schema;
import com.expediagroup.streamplatform.streamregistry.app.Stream;
import com.expediagroup.streamplatform.streamregistry.app.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.app.Zone;
import com.expediagroup.streamplatform.streamregistry.app.inputs.ConsumerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.ConsumerKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.DomainKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.InfrastructureKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.ProducerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.ProducerKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.SchemaKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.StreamBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.StreamKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.ZoneKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.mutation.MutationImpl;
import com.expediagroup.streamplatform.streamregistry.app.services.Services;

@Component
public class MyMutationImpl implements MutationImpl {

  private Services services;

  public MyMutationImpl(Services services) {
    this.services = services;
  }

  @Override
  public Domain insertDomain(DomainKeyInput key, SpecificationInput specification) {
    return services.getDomainService().create(asDomain(key, specification)).get();
  }

  @Override
  public Domain updateDomain(DomainKeyInput key, SpecificationInput specification) {
    return services.getDomainService().update(asDomain(key, specification)).get();
  }

  @Override
  public Domain upsertDomain(DomainKeyInput key, SpecificationInput specification) {
    return services.getDomainService().upsert(asDomain(key, specification)).get();
  }

  @Override
  public Boolean deleteDomain(DomainKeyInput key) {
    throw new UnsupportedOperationException("delete");
  }

  @Override
  public Domain updateDomainStatus(DomainKeyInput key, StatusInput status) {
    Domain domain = services.getDomainService().read(key.asDomainKey()).get();
    domain.setStatus(status.asStatus());
    return services.getDomainService().update(domain).get();
  }

  private Domain asDomain(DomainKeyInput key, SpecificationInput specification) {
    Domain domain = new Domain();
    domain.setKey(key.asDomainKey());
    domain.setSpecification(specification.asSpecification());
    return domain;
  }

  @Override
  public Schema insertSchema(SchemaKeyInput key, SpecificationInput specification) {
    return services.getSchemaService().create(asSchema(key, specification)).get();
  }

  @Override
  public Schema updateSchema(SchemaKeyInput key, SpecificationInput specification) {
    return services.getSchemaService().update(asSchema(key, specification)).get();
  }

  @Override
  public Schema upsertSchema(SchemaKeyInput key, SpecificationInput specification) {
    return services.getSchemaService().upsert(asSchema(key, specification)).get();
  }

  @Override
  public Boolean deleteSchema(SchemaKeyInput key) {
    throw new UnsupportedOperationException("delete");
  }

  @Override
  public Schema updateSchemaStatus(SchemaKeyInput key, StatusInput status) {
    Schema schema = services.getSchemaService().read(key.asSchemaKey()).get();
    schema.setStatus(status.asStatus());
    return services.getSchemaService().update(schema).get();
  }

  private Schema asSchema(SchemaKeyInput key, SpecificationInput specification) {
    Schema schema = new Schema();
    schema.setKey(key.asSchemaKey());
    schema.setSpecification(specification.asSpecification());
    return schema;
  }

  @Override
  public Stream insertStream(StreamKeyInput key, SpecificationInput specification, SchemaKeyInput schema) {
    return services.getStreamService().create(asStream(key, specification, schema)).get();
  }

  @Override
  public Stream updateStream(StreamKeyInput key, SpecificationInput specification) {
    return services.getStreamService().update(asStream(key, specification, null)).get();
  }

  @Override
  public Stream upsertStream(StreamKeyInput key, SpecificationInput specification, SchemaKeyInput schema) {
    return services.getStreamService().upsert(asStream(key, specification, schema)).get();
  }

  @Override
  public Boolean deleteStream(StreamKeyInput key) {
    throw new UnsupportedOperationException("deleteStream");
  }

  @Override
  public Stream updateStreamStatus(StreamKeyInput key, StatusInput status) {
    Stream stream = services.getStreamService().read(key.asStreamKey()).get();
    stream.setStatus(status.asStatus());
    return services.getStreamService().update(stream).get();
  }

  private Stream asStream(StreamKeyInput key, SpecificationInput specification, SchemaKeyInput schema) {
    Stream stream = new Stream();
    stream.setKey(key.asStreamKey());
    stream.setSpecification(specification.asSpecification());
    stream.setSchemaKey(schema.asSchemaKey());
    return stream;
  }

  @Override
  public Zone insertZone(ZoneKeyInput key, SpecificationInput specification) {
    return services.getZoneService().create(asZone(key, specification)).get();
  }

  @Override
  public Zone updateZone(ZoneKeyInput key, SpecificationInput specification) {
    return services.getZoneService().update(asZone(key, specification)).get();
  }

  @Override
  public Zone upsertZone(ZoneKeyInput key, SpecificationInput specification) {
    return services.getZoneService().upsert(asZone(key, specification)).get();
  }

  private Zone asZone(ZoneKeyInput key, SpecificationInput specification) {
    Zone zone = new Zone();
    //zone.setId(zk.toString()); //todo
    zone.setKey(key.asZoneKey());
    zone.setSpecification(specification.asSpecification());
    return zone;
  }

  @Override
  public Boolean deleteZone(ZoneKeyInput key) {
    throw new UnsupportedOperationException("deleteZone");
  }

  @Override
  public Zone updateZoneStatus(ZoneKeyInput key, StatusInput status) {
    Zone zone = services.getZoneService().read(key.asZoneKey()).get();
    zone.setStatus(status.asStatus());
    return services.getZoneService().update(zone).get();
  }

  @Override
  public Infrastructure insertInfrastructure(InfrastructureKeyInput key, SpecificationInput specification) {
    return null;
  }

  @Override
  public Infrastructure updateInfrastructure(InfrastructureKeyInput key, SpecificationInput specification) {
    return null;
  }

  @Override
  public Infrastructure upsertInfrastructure(InfrastructureKeyInput key, SpecificationInput specification) {
    return null;
  }

  @Override
  public Boolean deleteInfrastructure(InfrastructureKeyInput key) {
    throw new UnsupportedOperationException("delete");
  }

  @Override
  public Infrastructure updateInfrastructureStatus(InfrastructureKeyInput key, StatusInput status) {
    Infrastructure infrastructure = services.getInfrastructureService().read(key.asInfrastructureKey()).get();
    infrastructure.setStatus(status.asStatus());
    return services.getInfrastructureService().update(infrastructure).get();
  }

  @Override
  public Producer insertProducer(ProducerKeyInput key, SpecificationInput specification) {
    return services.getProducerService().create(asProducer(key, specification)).get();
  }

  @Override
  public Producer updateProducer(ProducerKeyInput key, SpecificationInput specification) {
    return services.getProducerService().update(asProducer(key, specification)).get();
  }

  @Override
  public Producer upsertProducer(ProducerKeyInput key, SpecificationInput specification) {
    return services.getProducerService().upsert(asProducer(key, specification)).get();
  }

  private Producer asProducer(ProducerKeyInput key, SpecificationInput specification) {
    Producer producer = new Producer();
    producer.setKey(key.asProducerKey());
    producer.setSpecification(specification.asSpecification());
    return producer;
  }

  @Override
  public Boolean deleteProducer(ProducerKeyInput key) {
    throw new UnsupportedOperationException("delete");
  }

  @Override
  public Producer updateProducerStatus(ProducerKeyInput key, StatusInput status) {
    Producer producer = services.getProducerService().read(key.asProducerKey()).get();
    producer.setStatus(status.asStatus());
    return services.getProducerService().update(producer).get();
  }

  @Override
  public Consumer insertConsumer(ConsumerKeyInput key, SpecificationInput specification) {
    return services.getConsumerService().create(asConsumer(key, specification)).get();
  }

  @Override
  public Consumer updateConsumer(ConsumerKeyInput key, SpecificationInput specification) {
    return services.getConsumerService().update(asConsumer(key, specification)).get();
  }

  @Override
  public Consumer upsertConsumer(ConsumerKeyInput key, SpecificationInput specification) {
    return services.getConsumerService().upsert(asConsumer(key, specification)).get();
  }

  private Consumer asConsumer(ConsumerKeyInput key, SpecificationInput specification) {
    Consumer consumer = new Consumer();
    consumer.setKey(key.asConsumerKey());
    consumer.setSpecification(specification.asSpecification());
    return consumer;
  }

  @Override
  public Boolean deleteConsumer(ConsumerKeyInput key) {
    throw new UnsupportedOperationException("delete");
  }

  @Override
  public Consumer updateConsumerStatus(ConsumerKeyInput key, StatusInput status) {
    Consumer consumer = services.getConsumerService().read(key.asConsumerKey()).get();
    consumer.setStatus(status.asStatus());
    return services.getConsumerService().update(consumer).get();
  }

  @Override
  public StreamBinding insertStreamBinding(StreamBindingKeyInput key, SpecificationInput specification) {
    return null;
  }

  @Override
  public StreamBinding updateStreamBinding(StreamBindingKeyInput key, SpecificationInput specification) {
    return null;
  }

  @Override
  public StreamBinding upsertStreamBinding(StreamBindingKeyInput key, SpecificationInput specification) {
    return null;
  }

  @Override
  public Boolean deleteStreamBinding(StreamBindingKeyInput key) {
    throw new UnsupportedOperationException("delete");
  }

  @Override
  public StreamBinding updateStreamBindingStatus(StreamBindingKeyInput key, StatusInput status) {
    StreamBinding streamBinding = services.getStreamBindingService().read(key.asStreamBindingKey()).get();
    streamBinding.setStatus(status.asStatus());
    return services.getStreamBindingService().update(streamBinding).get();
  }

  @Override
  public ProducerBinding insertProducerBinding(ProducerBindingKeyInput key, SpecificationInput specification) {
    return null;
  }

  @Override
  public ProducerBinding updateProducerBinding(ProducerBindingKeyInput key, SpecificationInput specification) {
    return null;
  }

  @Override
  public ProducerBinding upsertProducerBinding(ProducerBindingKeyInput key, SpecificationInput specification) {
    return null;
  }

  @Override
  public Boolean deleteProducerBinding(ProducerBindingKeyInput key) {
    throw new UnsupportedOperationException("delete");
  }

  @Override
  public ProducerBinding updateProducerBindingStatus(ProducerBindingKeyInput key, StatusInput status) {
    ProducerBinding producerBinding = services.getProducerBindingService().read(key.asProducerBindingKey()).get();
    producerBinding.setStatus(status.asStatus());
    return services.getProducerBindingService().update(producerBinding).get();
  }

  @Override
  public ConsumerBinding insertConsumerBinding(ConsumerBindingKeyInput key, SpecificationInput specification) {
    return null;
  }

  @Override
  public ConsumerBinding updateConsumerBinding(ConsumerBindingKeyInput key, SpecificationInput specification) {
    return null;
  }

  @Override
  public ConsumerBinding upsertConsumerBinding(ConsumerBindingKeyInput key, SpecificationInput specification) {
    return null;
  }

  @Override
  public Boolean deleteConsumerBinding(ConsumerBindingKeyInput key) {
    throw new UnsupportedOperationException("delete");
  }

  @Override
  public ConsumerBinding updateConsumerBindingStatus(ConsumerBindingKeyInput key, StatusInput status) {
    ConsumerBinding consumerBinding = services.getConsumerBindingService().read(key.asConsumerBindingKey()).get();
    consumerBinding.setStatus(status.asStatus());
    return services.getConsumerBindingService().update(consumerBinding).get();
  }
}

