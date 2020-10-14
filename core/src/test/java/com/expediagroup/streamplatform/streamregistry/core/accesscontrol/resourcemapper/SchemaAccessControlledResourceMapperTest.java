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

import static com.expediagroup.streamplatform.streamregistry.core.accesscontrol.domain.AcessControlledResourceFields.DOMAIN_NAME;
import static com.expediagroup.streamplatform.streamregistry.core.accesscontrol.domain.AcessControlledResourceFields.SCHEMA_NAME;
import static com.expediagroup.streamplatform.streamregistry.core.accesscontrol.domain.AcessControlledResourceFields.SPECIFICATION_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.expediagroup.streamplatform.streamregistry.core.accesscontrol.domain.AccessControlledResource;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;

public class SchemaAccessControlledResourceMapperTest {

  private SchemaAccessControlledResourceMapper underTest = new SchemaAccessControlledResourceMapper();

  @Test
  public void canMap() {
    Schema schema = createTestSchema();
    assertEquals(true, underTest.canMap(schema));
  }

  @Test
  public void map() {
    Schema schema = createTestSchema();
    Specification specification = schema.getSpecification();
    String specificationType = "spType";
    when(specification.getType()).thenReturn(specificationType);
    AccessControlledResource accessControlledResource = underTest.map(schema);

    assertEquals("domain", accessControlledResource.getKeys().get(DOMAIN_NAME));
    assertEquals("schemaName", accessControlledResource.getKeys().get(SCHEMA_NAME));
    assertEquals(specificationType, accessControlledResource.getAdditionalInfo().get(SPECIFICATION_TYPE));
  }

  private Schema createTestSchema() {
    SchemaKey schemaKey = new SchemaKey("domain", "schemaName");
    Specification specification = mock(Specification.class);
    Status status = mock(Status.class);
    return new Schema(schemaKey, specification, status);
  }
}