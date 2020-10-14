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

import static com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AcessControlledResourceFields.INFRASTRUCTURE_NAME;
import static com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AcessControlledResourceFields.SPECIFICATION_TYPE;
import static com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AcessControlledResourceFields.ZONE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AccessControlledResource;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.InfrastructureKey;

public class InfrastructureAccessControlledResourceMapperTest {

  private InfrastructureAccessControlledResourceMapper underTest = new InfrastructureAccessControlledResourceMapper();

  @Test
  public void canMap() {
    Infrastructure infrastructure = createTestInfrastructure();
    assertEquals(true, underTest.canMap(infrastructure));
  }

  @Test
  public void map() {
    Infrastructure infrastructure = createTestInfrastructure();
    Specification specification = infrastructure.getSpecification();
    String specificationType = "spType";
    when(specification.getType()).thenReturn(specificationType);
    AccessControlledResource accessControlledResource = underTest.map(infrastructure);

    assertEquals("zone", accessControlledResource.getKeys().get(ZONE_NAME));
    assertEquals("infrastructure", accessControlledResource.getKeys().get(INFRASTRUCTURE_NAME));
    assertEquals(specificationType, accessControlledResource.getAdditionalInfo().get(SPECIFICATION_TYPE));
  }

  private Infrastructure createTestInfrastructure() {
    InfrastructureKey infrastructureKey = new InfrastructureKey("zone", "infrastructure");
    Specification specification = mock(Specification.class);
    Status status = mock(Status.class);
    return new Infrastructure(infrastructureKey, specification, status);
  }
}