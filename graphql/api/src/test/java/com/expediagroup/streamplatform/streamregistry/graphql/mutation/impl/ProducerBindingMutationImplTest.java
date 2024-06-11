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

import com.expediagroup.streamplatform.streamregistry.core.services.ProducerBindingService;
import com.expediagroup.streamplatform.streamregistry.core.views.ProducerBindingView;
import com.expediagroup.streamplatform.streamregistry.graphql.InputHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.StateHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ProducerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;

@RunWith(MockitoJUnitRunner.class)
public class ProducerBindingMutationImplTest {

  @Mock
  private ProducerBindingService producerBindingService;

  @Mock
  private ProducerBindingView producerBindingView;

  private ProducerBindingMutationImpl producerBindingMutation;

  @Before
  public void before() throws Exception {
    producerBindingMutation = new ProducerBindingMutationImpl(producerBindingService, producerBindingView);
  }

  @Test
  public void deleteWithCheckExistEnabledWhenEntityExists() {
    ReflectionTestUtils.setField(producerBindingMutation, "checkExistEnabled", true);
    ProducerBindingKeyInput key = getProducerBindingInputKey();
    when(producerBindingView.get(any())).thenReturn(Optional.of(getProducer(key)));
    Boolean result = producerBindingMutation.delete(key);
    verify(producerBindingView, times(1)).get(key.asProducerBindingKey());
    verify(producerBindingService, times(1)).delete(any());
    assertTrue(result);
  }

  @Test
  public void deleteWithCheckExistEnabledWhenEntityDoesNotExist() {
    ReflectionTestUtils.setField(producerBindingMutation, "checkExistEnabled", true);
    ProducerBindingKeyInput key = getProducerBindingInputKey();
    when(producerBindingView.get(any())).thenReturn(Optional.empty());
    Boolean result = producerBindingMutation.delete(key);
    verify(producerBindingView, times(1)).get(key.asProducerBindingKey());
    verify(producerBindingService, times(0)).delete(any());
    assertTrue(result);
  }

  @Test
  public void deleteWithCheckExistDisabledWhenEntityExists() {
    ReflectionTestUtils.setField(producerBindingMutation, "checkExistEnabled", false);
    ProducerBindingKeyInput key = getProducerBindingInputKey();
    when(producerBindingView.get(any())).thenReturn(Optional.of(getProducer(key)));
    Boolean result = producerBindingMutation.delete(key);
    verify(producerBindingView, times(1)).get(key.asProducerBindingKey());
    verify(producerBindingService, times(1)).delete(getProducer(key));
    assertTrue(result);
  }

  @Test
  public void deleteWithCheckExistDisabledWhenEntityDoesNotExist() {
    ReflectionTestUtils.setField(producerBindingMutation, "checkExistEnabled", false);
    ProducerBindingKeyInput key = getProducerBindingInputKey();
    when(producerBindingView.get(any())).thenReturn(Optional.empty());
    Boolean result = producerBindingMutation.delete(key);
    verify(producerBindingView, times(1)).get(key.asProducerBindingKey());
    verify(producerBindingService, times(1)).delete(getProducer(key));
    assertTrue(result);
  }

  @Test
  public void updateStatusWithEntityStatusEnabled() {
    ReflectionTestUtils.setField(producerBindingMutation, "entityStatusEnabled", true);
    ProducerBindingKeyInput key = getProducerBindingInputKey();
    Optional<ProducerBinding> producerBinding = Optional.of(getProducer(key));
    StatusInput statusInput = InputHelper.statusInput();

    when(producerBindingView.get(any())).thenReturn(producerBinding);
    when(producerBindingService.updateStatus(any(), any())).thenReturn(producerBinding);

    ProducerBinding result = producerBindingMutation.updateStatus(key, statusInput);

    verify(producerBindingView, times(1)).get(key.asProducerBindingKey());
    verify(producerBindingService, times(1)).updateStatus(producerBinding.get(), statusInput.asStatus());
    assertEquals(producerBinding.get(), result);
  }

  @Test
  public void updateStatusWithEntityStatusDisabled() {
    ReflectionTestUtils.setField(producerBindingMutation, "entityStatusEnabled", false);
    ProducerBindingKeyInput key = getProducerBindingInputKey();
    Optional<ProducerBinding> producerBinding = Optional.of(getProducer(key));
    StatusInput statusInput = InputHelper.statusInput();

    when(producerBindingView.get(any())).thenReturn(producerBinding);

    ProducerBinding result = producerBindingMutation.updateStatus(key, statusInput);

    verify(producerBindingView, times(1)).get(key.asProducerBindingKey());
    verify(producerBindingService, never()).updateStatus(producerBinding.get(), statusInput.asStatus());
    assertEquals(producerBinding.get(), result);
  }

  private ProducerBindingKeyInput getProducerBindingInputKey() {
    return ProducerBindingKeyInput.builder()
      .streamDomain("domain")
      .streamName("stream")
      .streamVersion(1)
      .infrastructureZone("zone")
      .infrastructureName("infrastructure")
      .producerName("producer")
      .build();
  }

  private ProducerBinding getProducer(ProducerBindingKeyInput key) {
    return new ProducerBinding(key.asProducerBindingKey(), StateHelper.specification(), StateHelper.status());
  }
}
