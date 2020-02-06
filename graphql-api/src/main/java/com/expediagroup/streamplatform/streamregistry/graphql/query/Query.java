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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Getter
public class Query implements GraphQLQueryResolver {
  private final DomainQuery domain;
  private final SchemaQuery schema;
  private final StreamQuery stream;
  private final ProducerQuery producer;
  private final ConsumerQuery consumer;
  private final ZoneQuery zone;
  private final InfrastructureQuery infrastructure;
  private final StreamBindingQuery streamBinding;
  private final ProducerBindingQuery producerBinding;
  private final ConsumerBindingQuery consumerBinding;
}