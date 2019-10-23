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
package com.expediagroup.streamplatform.streamregistry.core.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Getter
public class Services {
  private final DomainService domainService;
  private final SchemaService schemaService;
  private final StreamService streamService;
  private final ZoneService zoneService;
  private final InfrastructureService infrastructureService;
  private final ProducerService producerService;
  private final ConsumerService consumerService;
  private final StreamBindingService streamBindingService;
  private final ProducerBindingService producerBindingService;
  private final ConsumerBindingService consumerBindingService;
}
