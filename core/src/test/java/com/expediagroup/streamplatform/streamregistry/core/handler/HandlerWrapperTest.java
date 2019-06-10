/**
 * Copyright (C) 2018-2019 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.core.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.handler.Handler;
import com.expediagroup.streamplatform.streamregistry.model.Configuration;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

@RunWith(MockitoJUnitRunner.class)
public class HandlerWrapperTest {
  Stream entity = Stream
      .builder()
      .name("stream")
      .configuration(Configuration
          .builder()
          .type("type")
          .properties(Map.of())
          .build())
      .build();
  Optional<Stream> existing = Optional.of(entity);
  @Mock
  private HandlerProvider<Stream> provider;
  @Mock
  private Handler<Stream> handler;
  private HandlerWrapper<Stream> underTest;

  @Before
  public void before() {
    when(provider.get(any())).thenReturn(handler);
    when(handler.handle(entity, existing)).thenReturn(entity);
    underTest = new HandlerWrapper<>(provider);
  }

  @Test
  public void test() {
    Stream result = underTest.handle(entity, existing);
    assertThat(result, is(entity));
  }
}
