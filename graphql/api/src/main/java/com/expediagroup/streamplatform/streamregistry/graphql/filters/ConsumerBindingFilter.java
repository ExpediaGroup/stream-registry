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
package com.expediagroup.streamplatform.streamregistry.graphql.filters;

import static com.expediagroup.streamplatform.streamregistry.graphql.filters.FilterUtility.matches;
import static com.expediagroup.streamplatform.streamregistry.graphql.filters.FilterUtility.matchesSpecification;

import java.util.function.Predicate;

import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.ConsumerBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;

public class ConsumerBindingFilter implements Predicate<ConsumerBinding> {

  private final ConsumerBindingKeyQuery keyQuery;
  private final SpecificationQuery specQuery;

  public ConsumerBindingFilter(ConsumerBindingKeyQuery keyQuery, SpecificationQuery specQuery) {
    this.keyQuery = keyQuery;
    this.specQuery = specQuery;
  }

  @Override
  public boolean test(ConsumerBinding d) {
    if (keyQuery != null) {
      if (!matches(d.getKey().getInfrastructureName(), keyQuery.getInfrastructureNameRegex())) {
        return false;
      }
      if (!matches(d.getKey().getInfrastructureZone(), keyQuery.getInfrastructureZoneRegex())) {
        return false;
      }
      if (!matches(d.getKey().getStreamDomain(), keyQuery.getStreamDomainRegex())) {
        return false;
      }
      if (!matches(d.getKey().getStreamName(), keyQuery.getStreamNameRegex())) {
        return false;
      }
      if (keyQuery.getStreamVersion() != null && d.getKey().getStreamVersion() != keyQuery.getStreamVersion()) {
        return false;
      }
      if (!matches(d.getKey().getConsumerName(), keyQuery.getConsumerNameRegex())) {
        return false;
      }
    }
    return matchesSpecification(d.getSpecification(), specQuery);
  }
}
