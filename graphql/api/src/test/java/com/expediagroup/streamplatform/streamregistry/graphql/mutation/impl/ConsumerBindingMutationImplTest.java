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

import com.expediagroup.streamplatform.streamregistry.core.services.ConsumerBindingService;
import com.expediagroup.streamplatform.streamregistry.core.views.ConsumerBindingView;
import com.expediagroup.streamplatform.streamregistry.graphql.InputHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.StateHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ConsumerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerBindingMutationImplTest {

  @Mock
  private ConsumerBindingService consumerBindingService;

  @Mock
  private ConsumerBindingView consumerBindingView;

  private ConsumerBindingMutationImpl consumerBindingMutation;

  @Before
  public void before() throws Exception {
    consumerBindingMutation = new ConsumerBindingMutationImpl(consumerBindingService, consumerBindingView);
  }

  @Test
  public void deleteWithCheckExistEnabledWhenEntityExists() {
    ReflectionTestUtils.setField(consumerBindingMutation, "checkExistEnabled", true);
    ConsumerBindingKeyInput key = getConsumerBindingInputKey();
    Optional<ConsumerBinding> consumerBinding = Optional.of(getConsumer(key));
    when(consumerBindingView.get(any())).thenReturn(consumerBinding);
    Boolean result = consumerBindingMutation.delete(key);
    verify(consumerBindingView, times(1)).get(key.asConsumerBindingKey());
    verify(consumerBindingService, times(1)).delete(consumerBinding.get());
    assertTrue(result);
  }

  @Test
  public void deleteWithCheckExistEnabledWhenEntityDoesNotExist() {
    ReflectionTestUtils.setField(consumerBindingMutation, "checkExistEnabled", true);
    ConsumerBindingKeyInput key = getConsumerBindingInputKey();
    Optional<ConsumerBinding> consumerBinding = Optional.of(getConsumer(key));
    when(consumerBindingView.get(any())).thenReturn(Optional.empty());
    Boolean result = consumerBindingMutation.delete(key);
    verify(consumerBindingView, times(1)).get(key.asConsumerBindingKey());
    verify(consumerBindingService, times(0)).delete(any());
    assertTrue(result);
  }

  @Test
  public void deleteWithCheckExistDisabledWhenEntityExists() {
    ReflectionTestUtils.setField(consumerBindingMutation, "checkExistEnabled", false);
    ConsumerBindingKeyInput key = getConsumerBindingInputKey();
    Optional<ConsumerBinding> consumerBinding = Optional.of(getConsumer(key));
    when(consumerBindingView.get(any())).thenReturn(consumerBinding);
    Boolean result = consumerBindingMutation.delete(key);
    verify(consumerBindingView, times(1)).get(key.asConsumerBindingKey());
    verify(consumerBindingService, times(1)).delete(consumerBinding.get());
    assertTrue(result);
  }

  @Test
  public void deleteWithCheckExistDisabledWhenEntityDoesNotExist() {
    ReflectionTestUtils.setField(consumerBindingMutation, "checkExistEnabled", false);
    ConsumerBindingKeyInput key = getConsumerBindingInputKey();
    Optional<ConsumerBinding> consumerBinding = Optional.of(getConsumer(key));
    when(consumerBindingView.get(any())).thenReturn(Optional.empty());
    Boolean result = consumerBindingMutation.delete(key);
    verify(consumerBindingView, times(1)).get(key.asConsumerBindingKey());
    verify(consumerBindingService, times(1)).delete(any());
    assertTrue(result);
  }

  @Test
  public void updateStatusWithEntityStatusEnabled() {
    ReflectionTestUtils.setField(consumerBindingMutation, "entityStatusEnabled", true);
    ConsumerBindingKeyInput key = getConsumerBindingInputKey();
    Optional<ConsumerBinding> consumerBinding = Optional.of(getConsumer(key));
    StatusInput statusInput = InputHelper.statusInput();

    when(consumerBindingView.get(any())).thenReturn(consumerBinding);
    when(consumerBindingService.updateStatus(any(), any())).thenReturn(consumerBinding);

    ConsumerBinding result = consumerBindingMutation.updateStatus(key, statusInput);

    verify(consumerBindingView, times(1)).get(key.asConsumerBindingKey());
    verify(consumerBindingService, times(1)).updateStatus(consumerBinding.get(), statusInput.asStatus());
    assertEquals(consumerBinding.get(), result);
  }

  @Test
  public void updateStatusWithEntityStatusDisabled() {
    ReflectionTestUtils.setField(consumerBindingMutation, "entityStatusEnabled", false);
    ConsumerBindingKeyInput key = getConsumerBindingInputKey();
    Optional<ConsumerBinding> consumerBinding = Optional.of(getConsumer(key));
    StatusInput statusInput = InputHelper.statusInput();

    when(consumerBindingView.get(any())).thenReturn(consumerBinding);

    ConsumerBinding result = consumerBindingMutation.updateStatus(key, statusInput);

    verify(consumerBindingView, times(1)).get(key.asConsumerBindingKey());
    verify(consumerBindingService, never()).updateStatus(consumerBinding.get(), statusInput.asStatus());
    assertEquals(consumerBinding.get(), result);
  }

  private ConsumerBindingKeyInput getConsumerBindingInputKey() {
    return ConsumerBindingKeyInput.builder()
      .streamDomain("domain")
      .streamName("stream")
      .streamVersion(1)
      .infrastructureZone("zone")
      .infrastructureName("infrastructure")
      .consumerName("consumer")
      .build();
  }

  private ConsumerBinding getConsumer(ConsumerBindingKeyInput key) {
    return new ConsumerBinding(key.asConsumerBindingKey(), StateHelper.specification(), StateHelper.status());
  }
}
