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
package com.expediagroup.streamplatform.streamregistry.app.resolvers;
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

import static com.expediagroup.streamplatform.streamregistry.model.scalars.ObjectNodeMapper.deserialise;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.Services;
import com.expediagroup.streamplatform.streamregistry.model.Status;

@Component
public class StatusResolver implements GraphQLResolver<Status> {

  private final Services services;

  public StatusResolver(Services services) {
    this.services = services;
  }

  public ObjectNode getAgentStatus(Status status) {
    return deserialise(status.getStatusJson());
  }
}