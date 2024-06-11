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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.expediagroup.streamplatform.streamregistry.core.services.StreamBindingService;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamBindingView;
import com.expediagroup.streamplatform.streamregistry.graphql.InputHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.StateHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StreamBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;

@RunWith(MockitoJUnitRunner.class)
public class StreamBindingMutationImplTest {

  @Mock
  private StreamBindingService streamBindingService;

  @Mock
  private StreamBindingView streamBindingView;

  private StreamBindingMutationImpl streamBindingMutation;

  @Before
  public void before() throws Exception {
    streamBindingMutation = new StreamBindingMutationImpl(streamBindingService, streamBindingView);
  }

  @Test
  public void deleteWithCheckExistEnabledWhenEntityExists() {
    ReflectionTestUtils.setField(streamBindingMutation, "checkExistEnabled", true);
    StreamBindingKeyInput key = getStreamBindingInputKey();
    when(streamBindingView.get(any())).thenReturn(Optional.of(getStreamBinding(key)));
    Boolean result = streamBindingMutation.delete(key);
    verify(streamBindingView, times(1)).get(key.asStreamBindingKey());
    verify(streamBindingService, times(1)).delete(any());
    assertTrue(result);
  }

  @Test
  public void deleteWithCheckExistEnabledWhenEntityDoesNotExist() {
    ReflectionTestUtils.setField(streamBindingMutation, "checkExistEnabled", true);
    StreamBindingKeyInput key = getStreamBindingInputKey();
    when(streamBindingView.get(any())).thenReturn(Optional.empty());
    Boolean result = streamBindingMutation.delete(key);
    verify(streamBindingView, times(1)).get(key.asStreamBindingKey());
    verify(streamBindingService, times(0)).delete(any());
    assertTrue(result);
  }

  @Test
  public void deleteWithCheckExistDisabledWhenEntiyExists() {
    ReflectionTestUtils.setField(streamBindingMutation, "checkExistEnabled", false);
    StreamBindingKeyInput key = getStreamBindingInputKey();
    when(streamBindingView.get(any())).thenReturn(Optional.of(getStreamBinding(key)));
    Boolean result = streamBindingMutation.delete(key);
    verify(streamBindingView, times(1)).get(key.asStreamBindingKey());
    verify(streamBindingService, times(1)).delete(any());
    assertTrue(result);
  }

  @Test
  public void deleteWithCheckExistDisabledWhenEntiyDoesNotExist() {
    ReflectionTestUtils.setField(streamBindingMutation, "checkExistEnabled", false);
    StreamBindingKeyInput key = getStreamBindingInputKey();
    when(streamBindingView.get(any())).thenReturn(Optional.empty());
    Boolean result = streamBindingMutation.delete(key);
    verify(streamBindingView, times(1)).get(key.asStreamBindingKey());
    verify(streamBindingService, times(1)).delete(any());
    assertTrue(result);
  }

  @Test
  public void updateStatusWithEntityStatusEnabled() {
    ReflectionTestUtils.setField(streamBindingMutation, "entityStatusEnabled", true);
    StreamBindingKeyInput key = getStreamBindingInputKey();
    Optional<StreamBinding> streamBinding = Optional.of(getStreamBinding(key));
    StatusInput statusInput = InputHelper.statusInput();

    when(streamBindingView.get(any())).thenReturn(streamBinding);
    when(streamBindingService.updateStatus(any(), any())).thenReturn(streamBinding);

    StreamBinding result = streamBindingMutation.updateStatus(key, statusInput);

    verify(streamBindingView, times(1)).get(key.asStreamBindingKey());
    verify(streamBindingService, times(1)).updateStatus(streamBinding.get(), statusInput.asStatus());
    assertEquals(streamBinding.get(), result);
  }

  @Test
  public void updateStatusWithEntityStatusDisabled() {
    ReflectionTestUtils.setField(streamBindingMutation, "entityStatusEnabled", false);
    StreamBindingKeyInput key = getStreamBindingInputKey();
    Optional<StreamBinding> streamBinding = Optional.of(getStreamBinding(key));
    StatusInput statusInput = InputHelper.statusInput();

    when(streamBindingView.get(any())).thenReturn(streamBinding);

    StreamBinding result = streamBindingMutation.updateStatus(key, statusInput);

    verify(streamBindingView, times(1)).get(key.asStreamBindingKey());
    verify(streamBindingService, never()).updateStatus(streamBinding.get(), statusInput.asStatus());
    assertEquals(streamBinding.get(), result);
  }

  private StreamBindingKeyInput getStreamBindingInputKey() {
    return StreamBindingKeyInput.builder()
      .streamDomain("domain")
      .streamName("stream")
      .streamVersion(1)
      .infrastructureZone("zone")
      .infrastructureName("infrastructure")
      .build();
  }

  private StreamBinding getStreamBinding(StreamBindingKeyInput key) {
    return new StreamBinding(key.asStreamBindingKey(), StateHelper.specification(), StateHelper.status());
  }
}
