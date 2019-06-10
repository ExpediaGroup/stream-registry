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

import static org.hamcrest.Matchers.is;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.handler.Handler;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

@RunWith(MockitoJUnitRunner.class)
public class HandlerProviderTest {

  @Mock
  private Handler<Stream> handler;

  private HandlerProvider<Stream> underTest;

  @Before
  public void before() {
    underTest = new HandlerProvider<>(Map.of("handler", handler));
  }

  @Test(expected = IllegalArgumentException.class)
  public void notExistsThrowsException() {
    underTest.get("not-exists");
  }

  @Test
  public void exists() {
    Handler<Stream> result = underTest.get("handler");
    Assert.assertThat(result, is(handler));
  }

}
