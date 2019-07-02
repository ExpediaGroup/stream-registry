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
package com.expediagroup.streamplatform.streamregistry.graphql.model;

import lombok.Builder;
import lombok.Value;

import com.expediagroup.streamplatform.streamregistry.model.Configuration;

@Value
@Builder
public class GraphQLConfiguration {
  String type;
  Iterable<GraphQLKeyValue> properties;

  public static GraphQLConfiguration fromDto(Configuration configuration) {
    return GraphQLConfiguration
        .builder()
        .type(configuration.getType())
        .properties(GraphQLKeyValue.fromDto(configuration.getProperties()))
        .build();
  }

  public static Configuration toDto(GraphQLConfiguration configuration) {
    if (configuration == null) {
      return null;
    }
    return Configuration
        .builder()
        .type(configuration.getType())
        .properties(GraphQLKeyValue.toDto(configuration.getProperties()))
        .build();
  }
}
