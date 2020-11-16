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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.StreamKey;

@RunWith(MockitoJUnitRunner.class)
public class StreamAndSchemaDiscovererTest {
  @Mock
  private EntityClient client;

  @InjectMocks
  private StreamAndSchemaDiscoverer underTest;

  private final DomainKey domainKey = new DomainKey("domain");
  private final SchemaKey schemaKey = new SchemaKey(domainKey, "schema");
  private final StreamKey streamKey1 = new StreamKey(domainKey, "stream", 1);
  private final StreamKey streamKey2 = new StreamKey(domainKey, "stream", 2);

  @Before
  public void before() {
    when(client.getStreamKeyWithSchemaKeys("domain", ".*", 1, null, null))
        .thenReturn(Map.of(streamKey1, schemaKey));
  }

  @Test
  public void schemaIsPresent() {
    when(client.getStreamKeyWithSchemaKeys(null, null, null, "domain", "schema"))
        .thenReturn(Map.of(streamKey1, schemaKey));

    Map<StreamKey, Optional<SchemaKey>> result = underTest.discover("domain", ".*", 1);

    assertThat(result.size(), is(1));
    assertThat(result.get(streamKey1).isPresent(), is(true));
  }

  @Test
  public void reusedSchemaIsRemoved() {
    when(client.getStreamKeyWithSchemaKeys(null, null, null, "domain", "schema"))
        .thenReturn(Map.of(streamKey1, schemaKey, streamKey2, schemaKey));

    Map<StreamKey, Optional<SchemaKey>> result = underTest.discover("domain", ".*", 1);

    assertThat(result.size(), is(1));
    assertThat(result.get(streamKey1).isPresent(), is(false));
  }
}
