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
package com.expediagroup.streamplatform.streamregistry.core.accesscontrol;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AccessType;

public class AccessTypeMapperTest {

  private AccessTypeMapper underTest = new AccessTypeMapper();

  @Test
  public void mapAccessType() {
    assertEquals(AccessType.CREATE, underTest.map("create"));
    assertEquals(AccessType.UPDATE, underTest.map("update"));
    assertEquals(AccessType.DELETE, underTest.map("delete"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void mapAccessType_invalidType() {
    assertEquals(AccessType.CREATE, underTest.map("invalid"));
  }
}