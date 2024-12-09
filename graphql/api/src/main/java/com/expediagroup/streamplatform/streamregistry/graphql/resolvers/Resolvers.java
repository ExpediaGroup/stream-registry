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
package com.expediagroup.streamplatform.streamregistry.graphql.resolvers;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.node.ObjectNode;

import com.expediagroup.streamplatform.streamregistry.graphql.GraphQLApiType;
import com.expediagroup.streamplatform.streamregistry.model.*;
import com.expediagroup.streamplatform.streamregistry.model.Process;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;
import graphql.kickstart.tools.GraphQLResolver;

interface Resolvers {
  interface DomainResolver extends EntityResolver<Domain>, GraphQLResolver<Domain>, GraphQLApiType {
    List<Schema> schemas(Domain domain);
  }

  interface SchemaResolver extends EntityResolver<Schema>, GraphQLResolver<Schema>, GraphQLApiType {
    Domain domain(Schema schema);
  }

  interface StreamResolver extends EntityResolver<Stream>, GraphQLResolver<Stream>, GraphQLApiType {
    Domain domain(Stream stream);

    Schema schema(Stream stream);
  }

  interface ZoneResolver extends EntityResolver<Zone>, GraphQLResolver<Zone>, GraphQLApiType {}

  interface InfrastructureResolver extends EntityResolver<Infrastructure>, GraphQLResolver<Infrastructure>, GraphQLApiType {
    Zone zone(Infrastructure infrastructure);
  }

  interface StreamBindingResolver extends EntityResolver<StreamBinding>, GraphQLResolver<StreamBinding>, GraphQLApiType {
    Stream stream(StreamBinding streamBinding);

    Infrastructure infrastructure(StreamBinding streamBinding);
  }

  interface ProducerResolver extends EntityResolver<Producer>, GraphQLResolver<Producer>, GraphQLApiType {
    Stream stream(Producer producer);

    Zone zone(Producer producer);

    ProducerBinding binding(Producer producer);
  }

  interface ConsumerResolver extends EntityResolver<Consumer>, GraphQLResolver<Consumer>, GraphQLApiType {
    Stream stream(Consumer consumer);

    Zone zone(Consumer consumer);

    ConsumerBinding binding(Consumer consumer);
  }

  interface ProcessResolver extends EntityResolver<Process>, GraphQLResolver<Process>, GraphQLApiType {
    Domain domain(Process process);
  }

  interface ConsumerBindingResolver extends EntityResolver<ConsumerBinding>, GraphQLResolver<ConsumerBinding>, GraphQLApiType {
    Consumer consumer(ConsumerBinding consumerBinding);

    StreamBinding binding(ConsumerBinding consumerBinding);
  }

  interface ProducerBindingResolver extends EntityResolver<ProducerBinding>, GraphQLResolver<ProducerBinding>, GraphQLApiType {
    Producer producer(ProducerBinding producerBinding);

    StreamBinding binding(ProducerBinding producerBinding);
  }

  interface ProcessBindingResolver extends EntityResolver<ProcessBinding>, GraphQLResolver<ProcessBinding>, GraphQLApiType {
    Process process(ProcessBinding processBinding);

    Domain domain(ProcessBinding processBinding);

    ZoneKey zone(ProcessBinding processBinding);
  }

  interface StatusResolver extends GraphQLResolver<Status>, GraphQLApiType {
    ObjectNode getAgentStatus(com.expediagroup.streamplatform.streamregistry.model.Status status);
  }

  interface EntityResolver<E extends Entity> {
    default Status status(E entity) {
      return Optional.ofNullable(entity.getStatus()).orElseGet(Status::new);
    }
  }
}
