/**
 * Copyright (C) 2018-2020 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.repository.kafka;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.repository.kafka.Converter.ConsumerBindingConverter;
import com.expediagroup.streamplatform.streamregistry.state.EntityView;
import com.expediagroup.streamplatform.streamregistry.state.EventSender;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerBindingRepositoryTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Mock private EntityView view;
  @Mock private EventSender sender;
  @Mock private ConsumerBindingConverter converter;

  @Spy
  @InjectMocks
  private ConsumerBindingRepository underTest;

  @Test
  public void findAllMatch() {
    ConsumerBinding consumerBinding = SampleModel.consumerBinding();
    doReturn(List.of(consumerBinding)).when(underTest).findAll();

    List<ConsumerBinding> result = underTest.findAll(SampleModel.consumerBinding());

    assertThat(result.size(), is(1));
    assertThat(result.get(0), is(SampleModel.consumerBinding()));
  }

  @Test
  public void findAllNoMatch() {
    ConsumerBinding consumerBinding = SampleModel.consumerBinding();
    consumerBinding.getKey().setConsumerName("different consumer");
    doReturn(List.of(consumerBinding)).when(underTest).findAll();

    List<ConsumerBinding> result = underTest.findAll(SampleModel.consumerBinding());

    assertThat(result.size(), is(0));
  }
}
