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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.expediagroup.streamplatform.streamregistry.core.services.ZoneService;
import com.expediagroup.streamplatform.streamregistry.core.views.ZoneView;
import com.expediagroup.streamplatform.streamregistry.graphql.InputHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.StateHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ZoneKeyInput;
import com.expediagroup.streamplatform.streamregistry.model.Zone;

@RunWith(MockitoJUnitRunner.class)
public class ZoneMutationImplTest {

  @Mock
  private ZoneService zoneService;

  @Mock
  private ZoneView zoneView;

  private ZoneMutationImpl zoneMutation;

  @Before
  public void before() throws Exception {
    zoneMutation = new ZoneMutationImpl(zoneService, zoneView);
  }

  @Test
  public void updateStatusWithEntityStatusEnabled() {
    ReflectionTestUtils.setField(zoneMutation, "entityStatusEnabled", true);
    ZoneKeyInput key = getZoneInputKey();
    Optional<Zone> zone = Optional.of(getZone(key));
    StatusInput statusInput = InputHelper.statusInput();

    when(zoneView.get(any())).thenReturn(zone);
    when(zoneService.updateStatus(any(), any())).thenReturn(zone);

    Zone result = zoneMutation.updateStatus(key, statusInput);

    verify(zoneView, times(1)).get(key.asZoneKey());
    verify(zoneService, times(1)).updateStatus(zone.get(), statusInput.asStatus());
    assertEquals(zone.get(), result);
  }

  @Test
  public void updateStatusWithEntityStatusDisabled() {
    ReflectionTestUtils.setField(zoneMutation, "entityStatusEnabled", false);
    ZoneKeyInput key = getZoneInputKey();
    Optional<Zone> zone = Optional.of(getZone(key));
    StatusInput statusInput = InputHelper.statusInput();

    when(zoneView.get(any())).thenReturn(zone);

    Zone result = zoneMutation.updateStatus(key, statusInput);

    verify(zoneView, times(1)).get(key.asZoneKey());
    verify(zoneService, never()).updateStatus(zone.get(), statusInput.asStatus());
    assertEquals(zone.get(), result);
  }

  private ZoneKeyInput getZoneInputKey() {
    return ZoneKeyInput.builder()
      .name("zone")
      .build();
  }

  private Zone getZone(ZoneKeyInput key) {
    return new Zone(key.asZoneKey(), StateHelper.specification(), StateHelper.status());
  }
}
