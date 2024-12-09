/**
 * Copyright (C) 2018-2024 Expedia, Inc.
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

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.ProcessBindingService;
import com.expediagroup.streamplatform.streamregistry.core.views.ProcessBindingView;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.*;
import com.expediagroup.streamplatform.streamregistry.graphql.mutation.ProcessBindingMutation;
import com.expediagroup.streamplatform.streamregistry.model.ProcessBinding;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProcessBindingMutationImpl implements ProcessBindingMutation {
  private final ProcessBindingService processBindingService;
  private final ProcessBindingView processBindingView;

  @Override
  public ProcessBinding insert(ProcessBindingKeyInput key, SpecificationInput specification,
                        ZoneKeyInput zone, List<ProcessInputStreamBindingInput> inputs, List<ProcessOutputStreamBindingInput> outputs) {
    return processBindingService.create(asProcessBinding(key, specification, zone, inputs, outputs)).get();
  }

  @Override
  public ProcessBinding update(ProcessBindingKeyInput key, SpecificationInput specification,
                        ZoneKeyInput zone, List<ProcessInputStreamBindingInput> inputs, List<ProcessOutputStreamBindingInput> outputs) {
    return processBindingService.update(asProcessBinding(key, specification, zone, inputs, outputs)).get();
  }

  @Override
  public ProcessBinding upsert(ProcessBindingKeyInput key, SpecificationInput specification,
                        ZoneKeyInput zone, List<ProcessInputStreamBindingInput> inputs, List<ProcessOutputStreamBindingInput> outputs) {
    ProcessBinding processBinding = asProcessBinding(key, specification, zone, inputs, outputs);
    if (!processBindingView.exists(processBinding.getKey())) {
      return processBindingService.create(processBinding).get();
    } else {
      return processBindingService.update(processBinding).get();
    }
  }

  @Override
  public Boolean delete(ProcessBindingKeyInput key) {
    processBindingView.get(key.asProcessBindingKey()).ifPresent(processBindingService::delete);
    return true;
  }

  @Override
  public ProcessBinding updateStatus(ProcessBindingKeyInput key, StatusInput status) {
    ProcessBinding processBinding = processBindingView.get(key.asProcessBindingKey()).get();
    return processBindingService.updateStatus(processBinding, status.asStatus()).get();
  }

  private ProcessBinding asProcessBinding(ProcessBindingKeyInput key, SpecificationInput specification,
                            ZoneKeyInput zone, List<ProcessInputStreamBindingInput> inputs, List<ProcessOutputStreamBindingInput> outputs) {
    ProcessBinding processBinding = new ProcessBinding();
    processBinding.setKey(key.asProcessBindingKey());
    processBinding.setSpecification(specification.asSpecification());
    processBinding.setZone(zone.asZoneKey());
    processBinding.setInputs(inputs.stream().map(ProcessInputStreamBindingInput::asProcessInputStreamBinding).collect(Collectors.toList()));
    processBinding.setOutputs(outputs.stream().map(ProcessOutputStreamBindingInput::asProcessOutputStreamBinding).collect(Collectors.toList()));
    maintainState(processBinding, processBindingView.get(processBinding.getKey()));
    return processBinding;
  }
}
