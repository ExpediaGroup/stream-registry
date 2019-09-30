package com.expediagroup.streamplatform.streamregistry.core.services;

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

import org.springframework.stereotype.Component;

@Component
public class Services {

  private DomainService domainService;
  private SchemaService schemaService;
  private StreamService streamService;
  private ZoneService zoneService;
  private InfrastructureService infrastructureService;
  private ProducerService producerService;
  private ConsumerService consumerService;
  private StreamBindingService streamBindingService;
  private ProducerBindingService producerBindingService;
  private ConsumerBindingService consumerBindingService;

  public Services(
      DomainService domainService,
      SchemaService schemaService,
      StreamService streamService,
      ZoneService zoneService,
      InfrastructureService infrastructureService,
      ProducerService producerService,
      ConsumerService consumerService,
      StreamBindingService streamBindingService,
      ProducerBindingService producerBindingService,
      ConsumerBindingService consumerBindingService
  ) {
    this.domainService = domainService;
    this.schemaService = schemaService;
    this.streamService = streamService;
    this.zoneService = zoneService;
    this.infrastructureService = infrastructureService;
    this.producerService = producerService;
    this.consumerService = consumerService;
    this.streamBindingService = streamBindingService;
    this.producerBindingService = producerBindingService;
    this.consumerBindingService = consumerBindingService;
  }

  public DomainService getDomainService() {
    return domainService;
  }

  public SchemaService getSchemaService() {
    return schemaService;
  }

  public StreamService getStreamService() {
    return streamService;
  }

  public ZoneService getZoneService() {
    return zoneService;
  }

  public InfrastructureService getInfrastructureService() {
    return infrastructureService;
  }

  public ProducerService getProducerService() {
    return producerService;
  }

  public ConsumerService getConsumerService() {
    return consumerService;
  }

  public StreamBindingService getStreamBindingService() {
    return streamBindingService;
  }

  public ProducerBindingService getProducerBindingService() {
    return producerBindingService;
  }

  public ConsumerBindingService getConsumerBindingService() {
    return consumerBindingService;
  }
}