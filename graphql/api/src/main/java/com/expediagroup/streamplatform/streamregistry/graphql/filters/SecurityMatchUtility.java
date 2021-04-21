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

import static com.expediagroup.streamplatform.streamregistry.graphql.filters.FilterUtility.matches;

import java.util.List;

import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SecurityQuery;
import com.expediagroup.streamplatform.streamregistry.model.Principal;
import com.expediagroup.streamplatform.streamregistry.model.Security;
import com.expediagroup.streamplatform.streamregistry.model.Specification;

public class SecurityMatchUtility {

  public static boolean matchesAllSecurityQueries(Specification specification, List<SecurityQuery> securityQueries) {
    return matchesAllSecurityQueries(specification == null ? null : specification.getSecurity(), securityQueries);
  }

  private static boolean matchesAllSecurityQueries(List<Security> security, List<SecurityQuery> securityQueries) {
    if (securityQueries == null || securityQueries.isEmpty()) {
      return true;
    }
    if (security == null || security.isEmpty()) {
      return false;
    }
    for (SecurityQuery securityQuery : securityQueries) {
      if (!matchesAnyRole(security, securityQuery)) {
        return false;
      }
    }
    return true;
  }

  private static boolean matchesAnyRole(List<Security> security, SecurityQuery securityQuery) {
    if (securityQuery == null) {
      return true;
    }
    for (Security sec: security) {
      if (matches(sec.getRole(), securityQuery.getRoleRegex())) {
        for (Principal principal: sec.getPrincipals()) {
          if (matchesPrincipal(principal, securityQuery)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private static boolean matchesPrincipal(Principal principal, SecurityQuery securityQuery) {
    return matches(principal.getName(), securityQuery.getPrincipalRegex());
  }
}
