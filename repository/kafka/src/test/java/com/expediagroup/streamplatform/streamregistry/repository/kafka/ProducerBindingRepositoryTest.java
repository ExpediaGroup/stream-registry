/**
 * Copyright (C) 2018-2021 Expedia, Inc.
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

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.repository.kafka.Converter.ProducerBindingConverter;
import com.expediagroup.streamplatform.streamregistry.state.EntityView;
import com.expediagroup.streamplatform.streamregistry.state.EventSender;

@RunWith(MockitoJUnitRunner.class)
public class ProducerBindingRepositoryTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Mock private EntityView view;
  @Mock private EventSender sender;
  @Mock private ProducerBindingConverter converter;

  @Spy
  @InjectMocks
  private ProducerBindingRepository underTest;

  @Test
  public void findAllMatch() {
    ProducerBinding producerBinding = SampleModel.producerBinding();
    doReturn(Collections.singletonList(producerBinding)).when(underTest).findAll();

    List<ProducerBinding> result = underTest.findAll(SampleModel.producerBinding());

    assertThat(result.size(), is(1));
    assertThat(result.get(0), is(SampleModel.producerBinding()));
  }

  @Test
  public void findAllNoMatch() {
    ProducerBinding producerBinding = SampleModel.producerBinding();
    producerBinding.getKey().setProducerName("different producer");
    doReturn(Collections.singletonList(producerBinding)).when(underTest).findAll();

    List<ProducerBinding> result = underTest.findAll(SampleModel.producerBinding());

    assertThat(result.size(), is(0));
  }
}
