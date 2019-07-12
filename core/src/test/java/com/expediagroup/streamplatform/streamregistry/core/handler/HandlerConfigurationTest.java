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

import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.handler.Handler;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

@RunWith(MockitoJUnitRunner.class)
public class HandlerConfigurationTest {
  private final HandlerConfiguration underTest = new HandlerConfiguration();
  @Mock
  private Handler<Domain> domainHandler1;
  @Mock
  private Handler<Domain> domainHandler2;
  @Mock
  private Handler<Schema> schemaHandler1;
  @Mock
  private Handler<Schema> schemaHandler2;
  @Mock
  private Handler<Stream> streamHandler1;
  @Mock
  private Handler<Stream> streamHandler2;

  @Test(expected = IllegalArgumentException.class)
  public void emptyDomainHandlersThrowsException() {
    underTest.schemaHandlerProvider(List.of());
  }

  @Test(expected = IllegalStateException.class)
  public void clashingDomainHandlersThrowsException() {
    when(domainHandler1.type()).thenReturn("type");
    when(domainHandler2.type()).thenReturn("type");
    underTest.domainHandlerProvider(List.of(domainHandler1, domainHandler2));
  }

  @Test
  public void domainHandlers() {
    when(domainHandler1.type()).thenReturn("type1");
    when(domainHandler2.type()).thenReturn("type2");
    underTest.domainHandlerProvider(List.of(domainHandler1, domainHandler2));
  }

  @Test(expected = IllegalArgumentException.class)
  public void emptySchemaHandlersThrowsException() {
    underTest.domainHandlerProvider(List.of());
  }

  @Test(expected = IllegalStateException.class)
  public void clashingSchemaHandlersThrowsException() {
    when(schemaHandler1.type()).thenReturn("type");
    when(schemaHandler2.type()).thenReturn("type");
    underTest.schemaHandlerProvider(List.of(schemaHandler1, schemaHandler2));
  }

  @Test
  public void schemaHandlers() {
    when(schemaHandler1.type()).thenReturn("type1");
    when(schemaHandler2.type()).thenReturn("type2");
    underTest.schemaHandlerProvider(List.of(schemaHandler1, schemaHandler2));
  }

  @Test(expected = IllegalArgumentException.class)
  public void emptyStreamHandlersThrowsException() {
    underTest.streamHandlerProvider(List.of());
  }

  @Test(expected = IllegalStateException.class)
  public void clashingStreamHandlersThrowsException() {
    when(streamHandler1.type()).thenReturn("type");
    when(streamHandler2.type()).thenReturn("type");
    underTest.streamHandlerProvider(List.of(streamHandler1, streamHandler2));
  }

  @Test
  public void streamHandlers() {
    when(streamHandler1.type()).thenReturn("type1");
    when(streamHandler2.type()).thenReturn("type2");
    underTest.streamHandlerProvider(List.of(streamHandler1, streamHandler2));
  }
}
