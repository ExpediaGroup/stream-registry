/**
 * Copyright (C) 2018-2025 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.graphql.query.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.ProcessService;
import com.expediagroup.streamplatform.streamregistry.graphql.filters.ProcessFilter;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ProcessKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.*;
import com.expediagroup.streamplatform.streamregistry.graphql.query.ProcessQuery;
import com.expediagroup.streamplatform.streamregistry.model.Process;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProcessQueryImpl implements ProcessQuery {
  private final ProcessService processService;

  @Override
  public Optional<Process> byKey(ProcessKeyInput key) {
    return processService.get(key.asProcessKey());
  }

  @Override
  public Iterable<Process> byQuery(ProcessKeyQuery key, SpecificationQuery specification,
                                   List<ZoneKeyQuery> zones, List<StreamKeyQuery> inputs, List<StreamKeyQuery> outputs) {
    return processService.findAll(new ProcessFilter(key, specification, zones, inputs, outputs));
  }
}
