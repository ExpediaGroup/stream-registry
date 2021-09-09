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
package com.expediagroup.streamplatform.streamregistry.repository.kafka;


import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.model.Principal;
import com.expediagroup.streamplatform.streamregistry.model.Process;
import com.expediagroup.streamplatform.streamregistry.model.ProcessBinding;
import com.expediagroup.streamplatform.streamregistry.model.ProcessInputStream;
import com.expediagroup.streamplatform.streamregistry.model.ProcessInputStreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.ProcessOutputStream;
import com.expediagroup.streamplatform.streamregistry.model.ProcessOutputStreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Security;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.Tag;
import com.expediagroup.streamplatform.streamregistry.model.Zone;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.DomainKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.InfrastructureKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProcessBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProcessKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;

final class SampleModel {
  private static final ObjectMapper mapper = new ObjectMapper();

  private SampleModel() {}

  static Specification specification() {
    Specification specification = new Specification();
    specification.setDescription("description");
    specification.setTags(Collections.singletonList(new Tag("name", "value")));
    specification.setType("type");
    specification.setConfiguration(mapper.createObjectNode());
    specification.setSecurity(Arrays.asList(
      new Security("admin", Arrays.asList(new Principal("user1")))
    ));
    specification.setFunction("function");
    return specification;
  }

  static Status status() {
    return new Status(mapper.createObjectNode());
  }

  static DomainKey domainKey() {
    DomainKey key = new DomainKey();
    key.setName("domain");
    return key;
  }

  static SchemaKey schemaKey() {
    SchemaKey key = new SchemaKey();
    key.setDomain("domain");
    key.setName("schema");
    return key;
  }

  static StreamKey streamKey() {
    StreamKey key = new StreamKey();
    key.setDomain("domain");
    key.setName("stream");
    key.setVersion(1);
    return key;
  }

  static ZoneKey zoneKey() {
    ZoneKey key = new ZoneKey();
    key.setName("zone");
    return key;
  }

  static InfrastructureKey infrastructureKey() {
    InfrastructureKey key = new InfrastructureKey();
    key.setZone("zone");
    key.setName("infrastructure");
    return key;
  }

  static ProducerKey producerKey() {
    ProducerKey key = new ProducerKey();
    key.setStreamDomain("domain");
    key.setStreamName("stream");
    key.setStreamVersion(1);
    key.setZone("zone");
    key.setName("producer");
    return key;
  }

  static ConsumerKey consumerKey() {
    ConsumerKey key = new ConsumerKey();
    key.setStreamDomain("domain");
    key.setStreamName("stream");
    key.setStreamVersion(1);
    key.setZone("zone");
    key.setName("consumer");
    return key;
  }

  static ProcessKey processKey() {
    ProcessKey key = new ProcessKey();
    key.setDomain("domain");
    key.setName("process");
    return key;
  }

  static StreamBindingKey streamBindingKey() {
    StreamBindingKey key = new StreamBindingKey();
    key.setStreamDomain("domain");
    key.setStreamName("stream");
    key.setStreamVersion(1);
    key.setInfrastructureZone("zone");
    key.setInfrastructureName("infrastructure");
    return key;
  }

  static ProducerBindingKey producerBindingKey() {
    ProducerBindingKey key = new ProducerBindingKey();
    key.setStreamDomain("domain");
    key.setStreamName("stream");
    key.setStreamVersion(1);
    key.setInfrastructureZone("zone");
    key.setInfrastructureName("infrastructure");
    key.setProducerName("producer");
    return key;
  }

  static ConsumerBindingKey consumerBindingKey() {
    ConsumerBindingKey key = new ConsumerBindingKey();
    key.setStreamDomain("domain");
    key.setStreamName("stream");
    key.setStreamVersion(1);
    key.setInfrastructureZone("zone");
    key.setInfrastructureName("infrastructure");
    key.setConsumerName("consumer");
    return key;
  }

  static ProcessBindingKey processBindingKey() {
    ProcessBindingKey key = new ProcessBindingKey();
    key.setDomainName("domain");
    key.setInfrastructureZone("zone");
    key.setProcessName("process");
    return key;
  }

  static Domain domain() {
    Domain entity = new Domain();
    entity.setKey(domainKey());
    entity.setSpecification(specification());
    entity.setStatus(status());
    return entity;
  }

  static Schema schema() {
    Schema entity = new Schema();
    entity.setKey(schemaKey());
    entity.setSpecification(specification());
    entity.setStatus(status());
    return entity;
  }

  static Stream stream() {
    Stream entity = new Stream();
    entity.setKey(streamKey());
    entity.setSpecification(specification());
    entity.setStatus(status());
    entity.setSchemaKey(schemaKey());
    return entity;
  }

  static Zone zone() {
    Zone entity = new Zone();
    entity.setKey(zoneKey());
    entity.setSpecification(specification());
    entity.setStatus(status());
    return entity;
  }

  static Infrastructure infrastructure() {
    Infrastructure entity = new Infrastructure();
    entity.setKey(infrastructureKey());
    entity.setSpecification(specification());
    entity.setStatus(status());
    return entity;
  }

  static Producer producer() {
    Producer entity = new Producer();
    entity.setKey(producerKey());
    entity.setSpecification(specification());
    entity.setStatus(status());
    return entity;
  }

  static Consumer consumer() {
    Consumer entity = new Consumer();
    entity.setKey(consumerKey());
    entity.setSpecification(specification());
    entity.setStatus(status());
    return entity;
  }

  static Process process() {
    Process entity = new Process();
    entity.setKey(processKey());
    entity.setSpecification(specification());
    entity.setZones(Collections.singletonList(zoneKey()));
    entity.setInputs(Collections.singletonList(new ProcessInputStream(streamKey(), mapper.createObjectNode())));
    entity.setOutputs(Collections.singletonList(new ProcessOutputStream(streamKey(), mapper.createObjectNode())));
    entity.setStatus(status());
    return entity;
  }

  static StreamBinding streamBinding() {
    StreamBinding entity = new StreamBinding();
    entity.setKey(streamBindingKey());
    entity.setSpecification(specification());
    entity.setStatus(status());
    return entity;
  }

  static ProducerBinding producerBinding() {
    ProducerBinding entity = new ProducerBinding();
    entity.setKey(producerBindingKey());
    entity.setSpecification(specification());
    entity.setStatus(status());
    return entity;
  }

  static ConsumerBinding consumerBinding() {
    ConsumerBinding entity = new ConsumerBinding();
    entity.setKey(consumerBindingKey());
    entity.setSpecification(specification());
    entity.setStatus(status());
    return entity;
  }

  static ProcessBinding processBinding() {
    ProcessBinding entity = new ProcessBinding();
    entity.setKey(processBindingKey());
    entity.setSpecification(specification());
    entity.setZone(zoneKey());
    entity.setInputs(Collections.singletonList(processInputStreamBinding()));
    entity.setOutputs(Collections.singletonList(processOutputStreamBinding()));
    entity.setStatus(status());
    return entity;
  }

  static ProcessInputStreamBinding processInputStreamBinding() {
    return new ProcessInputStreamBinding(streamBindingKey(), mapper.createObjectNode());
  }

  static ProcessOutputStreamBinding processOutputStreamBinding() {
    return new ProcessOutputStreamBinding(streamBindingKey(), mapper.createObjectNode());
  }
}
