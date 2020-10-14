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
package com.expediagroup.streamplatform.streamregistry.core.accesscontrol.resourcemapper;

import static com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AcessControlledResourceFields.DOMAIN_NAME;
import static com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AcessControlledResourceFields.SPECIFICATION_TYPE;
import static com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AcessControlledResourceFields.STREAM_NAME;
import static com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AcessControlledResourceFields.STREAM_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AccessControlledResource;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;

public class StreamAccessControlledResourceMapperTest {

  private StreamAccessControlledResourceMapper underTest = new StreamAccessControlledResourceMapper();

  @Test
  public void canMap() {
    Stream stream = createTestStream();
    assertEquals(true, underTest.canMap(stream));
  }

  @Test
  public void map() {
    Stream stream = createTestStream();
    Specification specification = stream.getSpecification();
    String specificationType = "spType";
    when(specification.getType()).thenReturn(specificationType);
    AccessControlledResource accessControlledResource = underTest.map(stream);

    assertEquals("domain", accessControlledResource.getKeys().get(DOMAIN_NAME));
    assertEquals("streamName", accessControlledResource.getKeys().get(STREAM_NAME));
    assertEquals("1", accessControlledResource.getKeys().get(STREAM_VERSION));
    assertEquals(specificationType, accessControlledResource.getAdditionalInfo().get(SPECIFICATION_TYPE));
  }

  private Stream createTestStream() {
    StreamKey streamKey = new StreamKey("domain", "streamName", 1);
    SchemaKey schemaKey = mock(SchemaKey.class);
    Specification specification = mock(Specification.class);
    Status status = mock(Status.class);
    return new Stream(streamKey, schemaKey, specification, status);
  }
}