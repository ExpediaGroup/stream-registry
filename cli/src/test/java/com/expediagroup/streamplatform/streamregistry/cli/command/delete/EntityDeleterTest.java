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
package com.expediagroup.streamplatform.streamregistry.cli.command.delete;

import static com.expediagroup.streamplatform.streamregistry.state.model.event.Event.specificationDeletion;
import static com.expediagroup.streamplatform.streamregistry.state.model.event.Event.statusDeletion;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.state.kafka.KafkaEventSender;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;

@RunWith(MockitoJUnitRunner.class)
public class EntityDeleterTest {
  @Mock private KafkaEventSender sender;

  private EntityDeleter underTest;

  @Before
  public void before() {
    underTest = new EntityDeleter(sender);
  }

  @Test
  public void test() {
    when(sender.send(any()))
        .thenReturn(completedFuture(null))
        .thenReturn(completedFuture(null));

    DomainKey domainKey = new DomainKey("domain");

    underTest.delete(domainKey);
    underTest.close();

    verify(sender).send(statusDeletion(domainKey, "agentStatus"));
    verify(sender).send(specificationDeletion(domainKey));
  }
}
