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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.expediagroup.streamplatform.streamregistry.core.services.ProcessService;
import com.expediagroup.streamplatform.streamregistry.core.views.ProcessView;
import com.expediagroup.streamplatform.streamregistry.graphql.InputHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.StateHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ProcessKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.model.Process;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;

@RunWith(MockitoJUnitRunner.class)
public class ProcessMutationImplTest {

  @Mock
  private ProcessService processService;

  @Mock
  private ProcessView processView;

  private ProcessMutationImpl processMutation;

  @Before
  public void before() throws Exception {
    processMutation = new ProcessMutationImpl(processService, processView);
  }

  @Test
  public void updateStatusWithEntityStatusEnabled() {
    ReflectionTestUtils.setField(processMutation, "entityStatusEnabled", true);
    ProcessKeyInput key = getProcessInputKey();
    Optional<Process> process = Optional.of(getProcess(key));
    StatusInput statusInput = InputHelper.statusInput();

    when(processView.get(any())).thenReturn(process);
    when(processService.updateStatus(any(), any())).thenReturn(process);

    Process result = processMutation.updateStatus(key, statusInput);

    verify(processView, times(1)).get(key.asProcessKey());
    verify(processService, times(1)).updateStatus(process.get(), statusInput.asStatus());
    assertEquals(process.get(), result);
  }

  @Test
  public void updateStatusWithEntityStatusDisabled() {
    ReflectionTestUtils.setField(processMutation, "entityStatusEnabled", false);
    ProcessKeyInput key = getProcessInputKey();
    Optional<Process> process = Optional.of(getProcess(key));
    StatusInput statusInput = InputHelper.statusInput();

    when(processView.get(any())).thenReturn(process);

    Process result = processMutation.updateStatus(key, statusInput);

    verify(processView, times(1)).get(key.asProcessKey());
    verify(processService, never()).updateStatus(process.get(), statusInput.asStatus());
    assertEquals(process.get(), result);
  }

  private ProcessKeyInput getProcessInputKey() {
    return ProcessKeyInput.builder()
      .domain("domain")
      .name("process")
      .build();
  }

  private Process getProcess(ProcessKeyInput key) {
    return new Process(
      key.asProcessKey(),
      StateHelper.specification(),
      List.of(new ZoneKey("zone")),
      List.of(),
      List.of(),
      StateHelper.status()
    );
  }
}
