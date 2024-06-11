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

import com.expediagroup.streamplatform.streamregistry.core.services.DomainService;
import com.expediagroup.streamplatform.streamregistry.core.views.DomainView;
import com.expediagroup.streamplatform.streamregistry.graphql.InputHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.StateHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.DomainKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.model.Domain;

@RunWith(MockitoJUnitRunner.class)
public class DomainMutationImplTest {

  @Mock
  private DomainService domainService;

  @Mock
  private DomainView domainView;

  private DomainMutationImpl domainMutation;

  @Before
  public void before() throws Exception {
    domainMutation = new DomainMutationImpl(domainService, domainView);
  }

  @Test
  public void updateStatusWithEntityStatusEnabled() {
    ReflectionTestUtils.setField(domainMutation, "entityStatusEnabled", true);
    DomainKeyInput key = getDomainInputKey();
    Optional<Domain> domain = Optional.of(getDomain(key));
    StatusInput statusInput = InputHelper.statusInput();

    when(domainView.get(any())).thenReturn(domain);
    when(domainService.updateStatus(any(), any())).thenReturn(domain);

    Domain result = domainMutation.updateStatus(key, statusInput);

    verify(domainView, times(1)).get(key.asDomainKey());
    verify(domainService, times(1)).updateStatus(domain.get(), statusInput.asStatus());
    assertEquals(domain.get(), result);
  }

  @Test
  public void updateStatusWithEntityStatusDisabled() {
    ReflectionTestUtils.setField(domainMutation, "entityStatusEnabled", false);
    DomainKeyInput key = getDomainInputKey();
    Optional<Domain> domain = Optional.of(getDomain(key));
    StatusInput statusInput = InputHelper.statusInput();

    when(domainView.get(any())).thenReturn(domain);

    Domain result = domainMutation.updateStatus(key, statusInput);

    verify(domainView, times(1)).get(key.asDomainKey());
    verify(domainService, never()).updateStatus(domain.get(), statusInput.asStatus());
    assertEquals(domain.get(), result);
  }

  private DomainKeyInput getDomainInputKey() {
    return DomainKeyInput.builder()
      .name("domain")
      .build();
  }

  private Domain getDomain(DomainKeyInput key) {
    return new Domain(key.asDomainKey(), StateHelper.specification(), StateHelper.status());
  }
}
