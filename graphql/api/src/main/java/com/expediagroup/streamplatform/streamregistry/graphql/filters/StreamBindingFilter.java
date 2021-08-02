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
import static com.expediagroup.streamplatform.streamregistry.graphql.filters.FilterUtility.matchesSpecification;

import java.util.function.Predicate;

import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.StreamBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamBindingKey;

public class StreamBindingFilter implements Predicate<StreamBinding> {

  private final StreamBindingKeyQuery keyQuery;
  private final SpecificationQuery specQuery;

  public StreamBindingFilter(StreamBindingKeyQuery keyQuery, SpecificationQuery specQuery) {
    this.keyQuery = keyQuery;
    this.specQuery = specQuery;
  }

  @Override
  public boolean test(StreamBinding d) {
    return matchesStreamBindingKey(d.getKey(), keyQuery) &&
      matchesSpecification(d.getSpecification(), specQuery);
  }

  public static boolean matchesStreamBindingKey(StreamBindingKey d, StreamBindingKeyQuery keyQuery) {
    if (keyQuery != null) {
      if (!matches(d.getInfrastructureName(), keyQuery.getInfrastructureNameRegex())) {
        return false;
      }
      if (!matches(d.getInfrastructureZone(), keyQuery.getInfrastructureZoneRegex())) {
        return false;
      }
      if (!matches(d.getStreamDomain(), keyQuery.getStreamDomainRegex())) {
        return false;
      }
      if (!matches(d.getStreamName(), keyQuery.getStreamNameRegex())) {
        return false;
      }
      if (keyQuery.getStreamVersion() != null && !d.getStreamVersion().equals(keyQuery.getStreamVersion())) {
        return false;
      }
    }

    return true;
  }
}
