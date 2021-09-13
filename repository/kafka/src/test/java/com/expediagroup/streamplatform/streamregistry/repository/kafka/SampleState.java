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


import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ConsumerBindingKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.InfrastructureKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ProcessBindingKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ProcessKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ProducerBindingKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.StreamBindingKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.StreamKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ZoneKey;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Principal;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.ProcessBindingSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.ProcessInputStream;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.ProcessInputStreamBinding;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.ProcessOutputStream;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.ProcessOutputStreamBinding;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.ProcessSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.StreamSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.status.DefaultStatus;
import com.expediagroup.streamplatform.streamregistry.state.model.status.StatusEntry;

final class SampleState {
  private static final ObjectMapper mapper = new ObjectMapper();

  private SampleState() {
  }

  private static DefaultSpecification specification() {
    return specificationWith("function");
  }

  private static DefaultSpecification specificationWith(String function) {
    return new DefaultSpecification(
      "description",
      Collections.singletonList(new com.expediagroup.streamplatform.streamregistry.state.model.specification.Tag("name", "value")),
      "type",
      mapper.createObjectNode(),
      new HashMap<String, List<Principal>>() {{
        put("admin", Collections.singletonList(new Principal("user1")));
      }},
      function
    );
  }

  static StreamSpecification streamSpecification() {
    return streamSpecificationWith("function");
  }

  static StreamSpecification streamSpecificationWith(String function) {
    return new StreamSpecification(
      "description",
      Collections.singletonList(new com.expediagroup.streamplatform.streamregistry.state.model.specification.Tag("name", "value")),
      "type",
      mapper.createObjectNode(),
      new HashMap<String, List<Principal>>() {{
        put("admin", Collections.singletonList(new Principal("user1")));
      }},
      function,
      schemaKey()
    );
  }

  static ProcessSpecification processSpecification() {
    return processSpecificationWith("function");
  }

  static ProcessSpecification processSpecificationWith(String function) {
    return new ProcessSpecification(
      Collections.singletonList(zoneKey()),
      "description",
      Collections.singletonList(new com.expediagroup.streamplatform.streamregistry.state.model.specification.Tag("name", "value")),
      "type",
      mapper.createObjectNode(),
      new HashMap<String, List<Principal>>() {{
        put("admin", Collections.singletonList(new Principal("user1")));
      }},
      function,
      Collections.singletonList(new ProcessInputStream(streamKey(), mapper.createObjectNode())),
      Collections.singletonList(new ProcessOutputStream(streamKey(), mapper.createObjectNode()))
    );
  }

  static ProcessBindingSpecification processBindingSpecification() {
    return processBindingSpecificationWith("function");
  }

  static ProcessBindingSpecification processBindingSpecificationWith(String function) {
    return new ProcessBindingSpecification(
      zoneKey(),
      "description",
      Collections.singletonList(new com.expediagroup.streamplatform.streamregistry.state.model.specification.Tag("name", "value")),
      "type",
      mapper.createObjectNode(),
      new HashMap<String, List<Principal>>() {{
        put("admin", Collections.singletonList(new Principal("user1")));
      }},
      function,
      Collections.singletonList(processInputStreamBinding()),
      Collections.singletonList(processOutputStreamBinding())
    );
  }

  static DefaultStatus status() {
    return new DefaultStatus().with(new StatusEntry("agentStatus", mapper.createObjectNode()));
  }

  static DomainKey domainKey() {
    return new DomainKey("domain");
  }

  static SchemaKey schemaKey() {
    return new SchemaKey(domainKey(), "schema");
  }

  static StreamKey streamKey() {
    return new StreamKey(domainKey(), "stream", 1);
  }

  static ZoneKey zoneKey() {
    return new ZoneKey("zone");
  }

  static InfrastructureKey infrastructureKey() {
    return new InfrastructureKey(zoneKey(), "infrastructure");
  }

  static ProducerKey producerKey() {
    return new ProducerKey(streamKey(), zoneKey(), "producer");
  }

  static ConsumerKey consumerKey() {
    return new ConsumerKey(streamKey(), zoneKey(), "consumer");
  }

  static ProcessKey processKey() {
    return new ProcessKey(domainKey(), "process");
  }

  static StreamBindingKey streamBindingKey() {
    return new StreamBindingKey(streamKey(), infrastructureKey());
  }

  static ProducerBindingKey producerBindingKey() {
    return new ProducerBindingKey(producerKey(), streamBindingKey());
  }

  static ConsumerBindingKey consumerBindingKey() {
    return new ConsumerBindingKey(consumerKey(), streamBindingKey());
  }

  static ProcessBindingKey processBindingKey() {
    return new ProcessBindingKey(processKey(), zoneKey());
  }

  static ProcessInputStreamBinding processInputStreamBinding() {
    return new ProcessInputStreamBinding(streamBindingKey(), mapper.createObjectNode());
  }

  static ProcessOutputStreamBinding processOutputStreamBinding() {
    return new ProcessOutputStreamBinding(streamBindingKey(), mapper.createObjectNode());
  }

  static Entity<DomainKey, DefaultSpecification> domain() {
    return new Entity<>(domainKey(), specification(), status());
  }

  static Entity<SchemaKey, DefaultSpecification> schema() {
    return new Entity<>(schemaKey(), specification(), status());
  }

  static Entity<SchemaKey, DefaultSpecification> schemaWithoutFunction() {
    return new Entity<>(schemaKey(), specificationWith(""), status());
  }

  static Entity<StreamKey, StreamSpecification> stream() {
    return new Entity<>(streamKey(), streamSpecification(), status());
  }

  static Entity<StreamKey, StreamSpecification> streamWithoutFunction() {
    return new Entity<>(streamKey(), streamSpecificationWith(""), status());
  }

  static Entity<ZoneKey, DefaultSpecification> zone() {
    return new Entity<>(zoneKey(), specification(), status());
  }

  static Entity<InfrastructureKey, DefaultSpecification> infrastructure() {
    return new Entity<>(infrastructureKey(), specification(), status());
  }

  static Entity<ProducerKey, DefaultSpecification> producer() {
    return new Entity<>(producerKey(), specification(), status());
  }

  static Entity<ConsumerKey, DefaultSpecification> consumer() {
    return new Entity<>(consumerKey(), specification(), status());
  }

  static Entity<ProcessKey, ProcessSpecification> process() {
    return new Entity<>(processKey(), processSpecification(), status());
  }

  static Entity<ProcessKey, ProcessSpecification> processWithoutFunction() {
    return new Entity<>(processKey(), processSpecificationWith(""), status());
  }

  static Entity<StreamBindingKey, DefaultSpecification> streamBinding() {
    return new Entity<>(streamBindingKey(), specification(), status());
  }

  static Entity<ProducerBindingKey, DefaultSpecification> producerBinding() {
    return new Entity<>(producerBindingKey(), specification(), status());
  }

  static Entity<ConsumerBindingKey, DefaultSpecification> consumerBinding() {
    return new Entity<>(consumerBindingKey(), specification(), status());
  }

  static Entity<ProcessBindingKey, ProcessBindingSpecification> processBinding() {
    return new Entity<>(processBindingKey(), processBindingSpecification(), status());
  }

  static Entity<ProcessBindingKey, ProcessBindingSpecification> processBindingWithoutFunction() {
    return new Entity<>(processBindingKey(), processBindingSpecificationWith(""), status());
  }

  static Event<DomainKey, DefaultSpecification> domainSpecificationDeletionEvent() {
    return Event.specificationDeletion(domainKey());
  }

  static Event<DomainKey, DefaultSpecification> domainSpecificationEvent() {
    return Event.specification(domainKey(), domain().getSpecification());
  }

  static Event<DomainKey, DefaultSpecification> domainStatusDeletionEvent() {
    return Event.statusDeletion(domainKey(), "statusName");
  }

  static Event<DomainKey, DefaultSpecification> domainStatusEvent() {
    return Event.status(domainKey(), new StatusEntry("agentStatus", mapper.createObjectNode()));
  }
}
