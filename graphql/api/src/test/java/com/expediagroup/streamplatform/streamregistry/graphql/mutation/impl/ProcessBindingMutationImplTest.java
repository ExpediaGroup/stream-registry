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

import com.expediagroup.streamplatform.streamregistry.core.services.ProcessBindingService;
import com.expediagroup.streamplatform.streamregistry.core.views.ProcessBindingView;
import com.expediagroup.streamplatform.streamregistry.graphql.InputHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.StateHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ProcessBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.model.ProcessBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;

@RunWith(MockitoJUnitRunner.class)
public class ProcessBindingMutationImplTest {

  @Mock
  private ProcessBindingService processBindingService;

  @Mock
  private ProcessBindingView processBindingView;

  private ProcessBindingMutationImpl processBindingMutation;

  @Before
  public void before() throws Exception {
    processBindingMutation = new ProcessBindingMutationImpl(processBindingService, processBindingView);
  }

  @Test
  public void updateStatusWithEntityStatusEnabled() {
    ReflectionTestUtils.setField(processBindingMutation, "entityStatusEnabled", true);
    ProcessBindingKeyInput key = getProcessBindingInputKey();
    Optional<ProcessBinding> processBinding = Optional.of(getProcessBinding(key));
    StatusInput statusInput = InputHelper.statusInput();

    when(processBindingView.get(any())).thenReturn(processBinding);
    when(processBindingService.updateStatus(any(), any())).thenReturn(processBinding);

    ProcessBinding result = processBindingMutation.updateStatus(key, statusInput);

    verify(processBindingView, times(1)).get(key.asProcessBindingKey());
    verify(processBindingService, times(1)).updateStatus(processBinding.get(), statusInput.asStatus());
    assertEquals(processBinding.get(), result);
  }

  @Test
  public void updateStatusWithEntityStatusDisabled() {
    ReflectionTestUtils.setField(processBindingMutation, "entityStatusEnabled", false);
    ProcessBindingKeyInput key = getProcessBindingInputKey();
    Optional<ProcessBinding> processBinding = Optional.of(getProcessBinding(key));
    StatusInput statusInput = InputHelper.statusInput();

    when(processBindingView.get(any())).thenReturn(processBinding);

    ProcessBinding result = processBindingMutation.updateStatus(key, statusInput);

    verify(processBindingView, times(1)).get(key.asProcessBindingKey());
    verify(processBindingService, never()).updateStatus(processBinding.get(), statusInput.asStatus());
    assertEquals(processBinding.get(), result);
  }

  private ProcessBindingKeyInput getProcessBindingInputKey() {
    return ProcessBindingKeyInput.builder()
      .domainName("domain")
      .processName("process")
      .infrastructureZone("zone")
      .build();
  }

  private ProcessBinding getProcessBinding(ProcessBindingKeyInput key) {
    return new ProcessBinding(
      key.asProcessBindingKey(),
      StateHelper.specification(),
      new ZoneKey("zone"),
      List.of(),
      List.of(),
      StateHelper.status()
    );
  }
}
