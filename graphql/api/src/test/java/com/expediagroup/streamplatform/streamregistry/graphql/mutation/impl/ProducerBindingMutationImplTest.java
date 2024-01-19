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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.expediagroup.streamplatform.streamregistry.core.services.ConsumerBindingService;
import com.expediagroup.streamplatform.streamregistry.core.views.ConsumerBindingView;
import com.expediagroup.streamplatform.streamregistry.graphql.StateHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ConsumerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;

@RunWith(MockitoJUnitRunner.class)
public class ProducerBindingMutationImplTest {

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
  public void deleteWithCheckExistEnabled() {
    ReflectionTestUtils.setField(consumerBindingMutation, "checkExistEnabled", true);
    ConsumerBindingKeyInput key = getConsumerBindingInputKey();
    when(consumerBindingView.get(any())).thenReturn(Optional.of(getConsumer(key)));
    Boolean result = consumerBindingMutation.delete(key);
    verify(consumerBindingService, times(1)).delete(any());
    verify(consumerBindingView, times(1)).get(any());
    assert (result);
  }

  @Test
  public void deleteWithCheckExistDisabled() {
    ReflectionTestUtils.setField(consumerBindingMutation, "checkExistEnabled", false);
    ConsumerBindingKeyInput key = getConsumerBindingInputKey();
    Boolean result = consumerBindingMutation.delete(key);
    verify(consumerBindingService, times(1)).delete(getConsumer(key));
    verify(consumerBindingView, times(0)).get(any());
    assert (result);
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
