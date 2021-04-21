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
package com.expediagroup.streamplatform.streamregistry.graphql.filters;

import static com.expediagroup.streamplatform.streamregistry.graphql.filters.SecurityMatchUtility.matchesAllSecurityQueries;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SecurityQuery;
import com.expediagroup.streamplatform.streamregistry.model.Principal;
import com.expediagroup.streamplatform.streamregistry.model.Security;
import com.expediagroup.streamplatform.streamregistry.model.Specification;

public class SecurityMatchUtilityTest {
  Specification specification = new Specification(
    "description",
    Collections.emptyList(),
    "type",
    new ObjectMapper().createObjectNode(),
    Arrays.asList(
      new Security("admin", Arrays.asList(new Principal("user1"))),
      new Security("creator", Arrays.asList(new Principal("user2"), new Principal("user3")))
    )
  );

  @Test
  public void matchesSecurity_role() {
    assertTrue(matchesAllSecurityQueries(
        specification,
        Collections.singletonList(SecurityQuery.builder().roleRegex("admin").build())
    ));
  }

  @Test
  public void matchesSecurity_principal() {
    assertTrue(matchesAllSecurityQueries(
      specification,
      Collections.singletonList(SecurityQuery.builder().principalRegex("user2").build())
    ));
  }

  @Test
  public void matchesSecurity_roleAndPrincipal() {
    assertTrue(matchesAllSecurityQueries(
      specification,
      Collections.singletonList(SecurityQuery.builder().roleRegex("admin").principalRegex("user1").build())
    ));
  }

  @Test
  public void notMatchesSecurity_roleNotPrincipal() {
    assertFalse(matchesAllSecurityQueries(
      specification,
      Collections.singletonList(SecurityQuery.builder().roleRegex("admin").principalRegex("user2").build())
    ));
  }

  @Test
  public void notMatchesSecurity_PrincipalNotRole() {
    assertFalse(matchesAllSecurityQueries(
      specification,
      Collections.singletonList(SecurityQuery.builder().roleRegex("owner").principalRegex("user2").build())
    ));
  }
}
