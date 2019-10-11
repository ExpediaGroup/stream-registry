/**
 * Copyright (C) 2018-2019 Expedia, Inc.
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
import static com.expediagroup.streamplatform.streamregistry.graphql.filters.TagMatchUtility.matchesAllTagQueries;

import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.model.Specification;

public class SpecificationMatchUtility {

  public static boolean matchesSpecification(Specification specification, SpecificationQuery specQuery) {
    if (specQuery != null) {

      if (!matches(specification.getDescription(), specQuery.getDescriptionRegex())) {
        return false;
      }

      if (!matches(specification.getType(), specQuery.getTypeRegex())) {
        return false;
      }

      return matchesAllTagQueries(specification, specQuery.getTags());
    }
    return true;
  }

}
