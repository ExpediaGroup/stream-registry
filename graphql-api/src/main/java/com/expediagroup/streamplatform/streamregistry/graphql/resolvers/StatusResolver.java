/**
 * Copyright (C) 2018-2020 Expedia, Inc.
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

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class StatusResolver implements Resolvers.StatusResolver {
  public ObjectNode getAgentStatus(Status status) {
    return status.getObjectNode();
  }
}
