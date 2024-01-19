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

import com.expediagroup.streamplatform.streamregistry.core.services.ProducerService;
import com.expediagroup.streamplatform.streamregistry.core.views.ProducerView;
import com.expediagroup.streamplatform.streamregistry.graphql.StateHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ProducerKeyInput;
import com.expediagroup.streamplatform.streamregistry.model.Producer;

@RunWith(MockitoJUnitRunner.class)
public class ProducerMutationImplTest {

  @Mock
  private ProducerService producerService;

  @Mock
  private ProducerView producerView;

  private ProducerMutationImpl producerMutation;

  @Before
  public void before() throws Exception {
    producerMutation = new ProducerMutationImpl(producerService, producerView);
  }

  @Test
  public void deleteWithCheckExistEnabled() {
    ReflectionTestUtils.setField(producerMutation, "checkExistEnabled", true);
    ProducerKeyInput key = getProducerInputKey();
    when(producerView.get(any())).thenReturn(Optional.of(getProducer(key)));
    Boolean result = producerMutation.delete(key);
    verify(producerService, times(1)).delete(any());
    verify(producerView, times(1)).get(any());
    assert (result);
  }

  @Test
  public void deleteWithCheckExistDisabled() {
    ProducerMutationImpl producerMutation = new ProducerMutationImpl(producerService, producerView);
    ReflectionTestUtils.setField(producerMutation, "checkExistEnabled", false);
    ProducerKeyInput key = getProducerInputKey();
    Boolean result = producerMutation.delete(key);
    verify(producerService, times(1)).delete(getProducer(key));
    verify(producerView, times(0)).get(any());
    assert (result);
  }

  private ProducerKeyInput getProducerInputKey() {
    return ProducerKeyInput.builder()
      .streamDomain("domain")
      .streamName("stream")
      .streamVersion(1)
      .zone("zone")
      .name("producer")
      .build();
  }

  private Producer getProducer(ProducerKeyInput key) {
    return new Producer(key.asProducerKey(), StateHelper.specification(), StateHelper.status());
  }
}
