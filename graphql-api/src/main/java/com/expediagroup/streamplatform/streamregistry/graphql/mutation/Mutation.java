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
package com.expediagroup.streamplatform.streamregistry.graphql.mutation;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.Services;

@Component
public class Mutation implements GraphQLMutationResolver {

  private final ConsumerMutation consumerMutation;
  private final ProducerMutation producerMutation;
  private final DomainMutation domainMutation;
  private final SchemaMutation schemaMutation;
  private final StreamMutation streamMutation;
  private final ZoneMutation zoneMutation;
  private final InfrastructureMutation infrastructureMutation;
  private final StreamBindingMutation streamBindingMutation;
  private final ProducerBindingMutation producerBindingMutation;
  private final ConsumerBindingMutation consumerBindingMutation;

  public Mutation(Services services) {
    consumerMutation = new ConsumerMutation(services.getConsumerService());
    producerMutation = new ProducerMutation(services.getProducerService());
    domainMutation = new DomainMutation(services.getDomainService());
    schemaMutation = new SchemaMutation(services.getSchemaService());
    streamMutation = new StreamMutation(services.getStreamService());
    zoneMutation = new ZoneMutation(services.getZoneService());
    infrastructureMutation = new InfrastructureMutation(services.getInfrastructureService());
    streamBindingMutation = new StreamBindingMutation(services.getStreamBindingService());
    producerBindingMutation = new ProducerBindingMutation(services.getProducerBindingService());
    consumerBindingMutation = new ConsumerBindingMutation(services.getConsumerBindingService());
  }

  public ConsumerMutation consumer() {
    return consumerMutation;
  }

  public DomainMutation domain() {
    return domainMutation;
  }

  public SchemaMutation schema() {
    return schemaMutation;
  }

  public StreamMutation stream() {
    return streamMutation;
  }

  public ZoneMutation zone() {
    return zoneMutation;
  }

  public InfrastructureMutation infrastructure() {
    return infrastructureMutation;
  }

  public StreamBindingMutation streamBinding() {
    return streamBindingMutation;
  }

  public ProducerMutation producer() {
    return producerMutation;
  }

  public ProducerBindingMutation producerBinding() {
    return producerBindingMutation;
  }

  public ConsumerBindingMutation consumerBinding() {
    return consumerBindingMutation;
  }
}
