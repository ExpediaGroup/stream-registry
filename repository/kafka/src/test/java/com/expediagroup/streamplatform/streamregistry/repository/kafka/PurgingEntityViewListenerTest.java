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

import static com.expediagroup.streamplatform.streamregistry.repository.kafka.SampleState.domainSpecificationDeletionEvent;
import static com.expediagroup.streamplatform.streamregistry.repository.kafka.SampleState.domainSpecificationEvent;
import static com.expediagroup.streamplatform.streamregistry.repository.kafka.SampleState.domainStatusDeletionEvent;
import static com.expediagroup.streamplatform.streamregistry.repository.kafka.SampleState.domainStatusEvent;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.state.EntityView;

@RunWith(MockitoJUnitRunner.class)
public class PurgingEntityViewListenerTest {

  @Mock
  private EntityView view;
  private PurgingEntityViewListener purgingEntityViewListener;
  
  @Before
  public void before() {
    purgingEntityViewListener = new PurgingEntityViewListener(view);
  }

  @Test
  public void purgeDeleteEvents() {
    purgingEntityViewListener.onEvent(null, domainSpecificationDeletionEvent());

    verify(view).purgeDeleted(domainSpecificationDeletionEvent().getKey());
  }

  @Test
  public void ignoreNonDeleteEvents() {
    purgingEntityViewListener.onEvent(null, domainSpecificationEvent());
    purgingEntityViewListener.onEvent(null, domainStatusDeletionEvent());
    purgingEntityViewListener.onEvent(null, domainStatusEvent());

    verifyNoInteractions(view);
  }
}