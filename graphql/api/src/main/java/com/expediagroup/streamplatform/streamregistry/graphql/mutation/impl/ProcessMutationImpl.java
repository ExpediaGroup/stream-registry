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
package com.expediagroup.streamplatform.streamregistry.graphql.mutation.impl;

import static com.expediagroup.streamplatform.streamregistry.graphql.StateHelper.maintainState;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.ProcessService;
import com.expediagroup.streamplatform.streamregistry.core.views.ProcessView;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.*;
import com.expediagroup.streamplatform.streamregistry.graphql.mutation.ProcessMutation;
import com.expediagroup.streamplatform.streamregistry.model.Process;

@Component
@RequiredArgsConstructor
public class ProcessMutationImpl implements ProcessMutation {
  private final ProcessService processService;
  private final ProcessView processView;

  @Override
  public Process insert(ProcessKeyInput key, SpecificationInput specification,
                        List<ZoneKeyInput> zones, List<ProcessInInput> inputs, List<ProcessOutInput> outputs) {
    return processService.create(asProcess(key, specification, zones, inputs, outputs)).get();
  }

  @Override
  public Process update(ProcessKeyInput key, SpecificationInput specification,
                        List<ZoneKeyInput> zones, List<ProcessInInput> inputs, List<ProcessOutInput> outputs) {
    return processService.update(asProcess(key, specification, zones, inputs, outputs)).get();
  }

  @Override
  public Process upsert(ProcessKeyInput key, SpecificationInput specification,
                        List<ZoneKeyInput> zones, List<ProcessInInput> inputs, List<ProcessOutInput> outputs) {
    Process stream = asProcess(key, specification, zones, inputs, outputs);
    if (!processView.get(stream.getKey()).isPresent()) {
      return processService.create(stream).get();
    } else {
      return processService.update(stream).get();
    }
  }

  @Override
  public Boolean delete(ProcessKeyInput key) {
    processView.get(key.asProcessKey()).ifPresent(processService::delete);
    return true;
  }

  @Override
  public Process updateStatus(ProcessKeyInput key, StatusInput status) {
    Process stream = processView.get(key.asProcessKey()).get();
    return processService.updateStatus(stream, status.asStatus()).get();
  }

  private Process asProcess(ProcessKeyInput key, SpecificationInput specification,
                            List<ZoneKeyInput> zones, List<ProcessInInput> inputs, List<ProcessOutInput> outputs) {
    Process process = new Process();
    process.setKey(key.asProcessKey());
    process.setSpecification(specification.asSpecification());
    process.setZones(zones.stream().map(ZoneKeyInput::asZoneKey).collect(Collectors.toList()));
    process.setInputs(inputs.stream().map(ProcessInInput::asProcessInput).collect(Collectors.toList()));
    process.setOutputs(outputs.stream().map(ProcessOutInput::asProcessOutput).collect(Collectors.toList()));
    maintainState(process, processView.get(process.getKey()));
    return process;
  }
}
