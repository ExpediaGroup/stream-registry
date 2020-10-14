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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import com.expediagroup.streamplatform.streamregistry.core.accesscontrol.domain.AccessControlledResource;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.DomainKey;

public class DomainAccessControlledResourceMapperTest {

  private DomainAccessControlledResourceMapper underTest = new DomainAccessControlledResourceMapper();

  @Test
  public void canMap() {
    Domain domain = createTestDomain();
    assertEquals(true, underTest.canMap(domain));
  }

  @Test
  public void map() {
    Domain domain = createTestDomain();
    AccessControlledResource accessControlledResource = underTest.map(domain);

    assertEquals("domain", accessControlledResource.getKeys().get(DOMAIN_NAME));
  }

  private Domain createTestDomain() {
    DomainKey domainKey = new DomainKey("domain");
    Specification specification = mock(Specification.class);
    Status status = mock(Status.class);
    return new Domain(domainKey, specification, status);
  }
}