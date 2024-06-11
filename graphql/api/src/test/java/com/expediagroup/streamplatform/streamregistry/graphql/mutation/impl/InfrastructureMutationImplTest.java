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

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.expediagroup.streamplatform.streamregistry.core.services.InfrastructureService;
import com.expediagroup.streamplatform.streamregistry.core.views.InfrastructureView;
import com.expediagroup.streamplatform.streamregistry.graphql.InputHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.StateHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.InfrastructureKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;

@RunWith(MockitoJUnitRunner.class)
public class InfrastructureMutationImplTest {

  @Mock
  private InfrastructureService infrastructureService;

  @Mock
  private InfrastructureView infrastructureView;

  private InfrastructureMutationImpl infrastructureMutation;

  @Before
  public void before() throws Exception {
    infrastructureMutation = new InfrastructureMutationImpl(infrastructureService, infrastructureView);
  }

  @Test
  public void updateStatusWithEntityStatusEnabled() {
    ReflectionTestUtils.setField(infrastructureMutation, "entityStatusEnabled", true);
    InfrastructureKeyInput key = getInfrastructureInputKey();
    Optional<Infrastructure> infrastructure = Optional.of(getInfrastructure(key));
    StatusInput statusInput = InputHelper.statusInput();

    when(infrastructureView.get(any())).thenReturn(infrastructure);
    when(infrastructureService.updateStatus(any(), any())).thenReturn(infrastructure);

    Infrastructure result = infrastructureMutation.updateStatus(key, statusInput);

    verify(infrastructureView, times(1)).get(key.asInfrastructureKey());
    verify(infrastructureService, times(1)).updateStatus(infrastructure.get(), statusInput.asStatus());
    assertEquals(infrastructure.get(), result);
  }

  @Test
  public void updateStatusWithEntityStatusDisabled() {
    ReflectionTestUtils.setField(infrastructureMutation, "entityStatusEnabled", false);
    InfrastructureKeyInput key = getInfrastructureInputKey();
    Optional<Infrastructure> infrastructure = Optional.of(getInfrastructure(key));
    StatusInput statusInput = InputHelper.statusInput();

    when(infrastructureView.get(any())).thenReturn(infrastructure);

    Infrastructure result = infrastructureMutation.updateStatus(key, statusInput);

    verify(infrastructureView, times(1)).get(key.asInfrastructureKey());
    verify(infrastructureService, never()).updateStatus(infrastructure.get(), statusInput.asStatus());
    assertEquals(infrastructure.get(), result);
  }

  private InfrastructureKeyInput getInfrastructureInputKey() {
    return InfrastructureKeyInput.builder()
      .name("infrastructure")
      .zone("zone")
      .build();
  }

  private Infrastructure getInfrastructure(InfrastructureKeyInput key) {
    return new Infrastructure(key.asInfrastructureKey(), StateHelper.specification(), StateHelper.status());
  }
}
