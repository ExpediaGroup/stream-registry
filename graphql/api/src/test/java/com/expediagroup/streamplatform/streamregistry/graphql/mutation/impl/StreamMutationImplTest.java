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

import com.expediagroup.streamplatform.streamregistry.core.services.StreamService;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamView;
import com.expediagroup.streamplatform.streamregistry.graphql.InputHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.StateHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StreamKeyInput;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

@RunWith(MockitoJUnitRunner.class)
public class StreamMutationImplTest {

  @Mock
  private StreamService streamService;

  @Mock
  private StreamView streamView;
  private StreamMutationImpl streamMutation;

  @Before
  public void before() throws Exception {
    streamMutation = new StreamMutationImpl(streamService, streamView);
  }

  @Test
  public void deleteWithCheckExistEnabledWhenEntityExists() {
    ReflectionTestUtils.setField(streamMutation, "checkExistEnabled", true);
    StreamKeyInput key = getStreamInputKey();
    when(streamView.get(any())).thenReturn(Optional.of(getStream(key)));
    Boolean result = streamMutation.delete(key);
    verify(streamView, times(1)).get(key.asStreamKey());
    verify(streamService, times(1)).delete(any());
    assertTrue(result);
  }

  @Test
  public void deleteWithCheckExistEnabledWhenEntityDoesNotExist() {
    ReflectionTestUtils.setField(streamMutation, "checkExistEnabled", true);
    StreamKeyInput key = getStreamInputKey();
    when(streamView.get(any())).thenReturn(Optional.empty());
    Boolean result = streamMutation.delete(key);
    verify(streamView, times(1)).get(key.asStreamKey());
    verify(streamService, times(0)).delete(any());
    assertTrue(result);
  }

  @Test
  public void deleteWithCheckExistDisabledWhenEntityExists() {
    ReflectionTestUtils.setField(streamMutation, "checkExistEnabled", false);
    StreamKeyInput key = getStreamInputKey();
    when(streamView.get(any())).thenReturn(Optional.of(getStream(key)));
    Boolean result = streamMutation.delete(key);
    verify(streamView, times(1)).get(key.asStreamKey());
    verify(streamService, times(1)).delete(any());
    assertTrue(result);
  }

  @Test
  public void deleteWithCheckExistDisabledWhenEntityDoesNotExist() {
    ReflectionTestUtils.setField(streamMutation, "checkExistEnabled", false);
    StreamKeyInput key = getStreamInputKey();
    when(streamView.get(any())).thenReturn(Optional.empty());
    Boolean result = streamMutation.delete(key);
    verify(streamView, times(1)).get(key.asStreamKey());
    verify(streamService, times(1)).delete(any());
    assertTrue(result);
  }

  @Test
  public void updateStatusWithEntityStatusEnabled() {
    ReflectionTestUtils.setField(streamMutation, "entityStatusEnabled", true);
    StreamKeyInput key = getStreamInputKey();
    Optional<Stream> stream = Optional.of(getStream(key));
    StatusInput statusInput = InputHelper.statusInput();

    when(streamView.get(any())).thenReturn(stream);
    when(streamService.updateStatus(any(), any())).thenReturn(stream);

    Stream result = streamMutation.updateStatus(key, statusInput);

    verify(streamView, times(1)).get(key.asStreamKey());
    verify(streamService, times(1)).updateStatus(stream.get(), statusInput.asStatus());
    assertEquals(stream.get(), result);
  }

  @Test
  public void updateStatusWithEntityStatusDisabled() {
    ReflectionTestUtils.setField(streamMutation, "entityStatusEnabled", false);
    StreamKeyInput key = getStreamInputKey();
    Optional<Stream> stream = Optional.of(getStream(key));
    StatusInput statusInput = InputHelper.statusInput();

    when(streamView.get(any())).thenReturn(stream);

    Stream result = streamMutation.updateStatus(key, statusInput);

    verify(streamView, times(1)).get(key.asStreamKey());
    verify(streamService, never()).updateStatus(stream.get(), statusInput.asStatus());
    assertEquals(stream.get(), result);
  }

  private StreamKeyInput getStreamInputKey() {
    return StreamKeyInput.builder()
      .domain("domain")
      .name("name")
      .version(1)
      .build();
  }

  private Stream getStream(StreamKeyInput key) {
    return new Stream(key.asStreamKey(), StateHelper.schemaKey(), StateHelper.specification(), StateHelper.status());
  }
}
