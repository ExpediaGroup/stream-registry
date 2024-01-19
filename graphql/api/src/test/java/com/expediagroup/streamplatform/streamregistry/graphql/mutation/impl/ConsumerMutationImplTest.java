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

import com.expediagroup.streamplatform.streamregistry.core.services.ConsumerService;
import com.expediagroup.streamplatform.streamregistry.core.views.ConsumerView;
import com.expediagroup.streamplatform.streamregistry.graphql.StateHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ConsumerKeyInput;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerMutationImplTest {

  @Mock
  private ConsumerService consumerService;

  @Mock
  private ConsumerView consumerView;

  private ConsumerMutationImpl consumerMutation;

  @Before
  public void before() throws Exception {
    consumerMutation = new ConsumerMutationImpl(consumerService, consumerView);
  }

  @Test
  public void deleteWithCheckExistEnabled() {
    ReflectionTestUtils.setField(consumerMutation, "checkExistEnabled", true);
    ConsumerKeyInput key = getConsumerInputKey();
    when(consumerView.get(any())).thenReturn(Optional.of(getConsumer(key)));
    Boolean result = consumerMutation.delete(key);
    verify(consumerService, times(1)).delete(any());
    verify(consumerView, times(1)).get(any());
    assert (result);
  }

  @Test
  public void deleteWithCheckExistDisabled() {
    ReflectionTestUtils.setField(consumerMutation, "checkExistEnabled", false);
    ConsumerKeyInput key = getConsumerInputKey();
    Boolean result = consumerMutation.delete(key);
    verify(consumerService, times(1)).delete(getConsumer(key));
    verify(consumerView, times(0)).get(any());
    assert (result);
  }

  private ConsumerKeyInput getConsumerInputKey() {
    return ConsumerKeyInput.builder()
      .streamDomain("domain")
      .streamName("stream")
      .streamVersion(1)
      .zone("zone")
      .name("consumer")
      .build();
  }

  private Consumer getConsumer(ConsumerKeyInput key) {
    return new Consumer(key.asConsumerKey(), StateHelper.specification(), StateHelper.status());
  }
}
