/**
 * Copyright (C) 2016-2019 Expedia Inc.
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

package com.expediagroup.streamplatform.streamregistry.app.filters.queryFilters;

import static com.expediagroup.streamplatform.streamregistry.app.filters.queryFilters.SpecificationMatchUtility.matchesSpecification;

import com.expediagroup.streamplatform.streamregistry.app.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.app.filters.Filter;
import com.expediagroup.streamplatform.streamregistry.app.queries.ProducerBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.app.queries.SpecificationQuery;

public class ProducerBindingFilter implements Filter<ProducerBinding> {

  private final ProducerBindingKeyQuery keyQuery;
  private final SpecificationQuery specQuery;

  public ProducerBindingFilter(ProducerBindingKeyQuery keyQuery, SpecificationQuery specQuery){
    this.keyQuery = keyQuery;
    this.specQuery = specQuery;
  }

  public boolean matches(ProducerBinding d) {
    if (keyQuery != null) {
      if (!d.getKey().getInfrastructureName().matches(keyQuery.getInfrastructureNameRegex())) {
        return false;
      }
      if (!d.getKey().getInfrastructureZone().matches(keyQuery.getInfrastructureZoneRegex())) {
        return false;
      }
      if (!d.getKey().getStreamDomain().matches(keyQuery.getStreamDomainRegex())) {
        return false;
      }
      if (!d.getKey().getStreamName().matches(keyQuery.getStreamNameRegex())) {
        return false;
      }
      if (keyQuery.getStreamVersion() != null && d.getKey().getStreamVersion() != keyQuery.getStreamVersion()) {
        return false;
      }
    }
    return matchesSpecification(d.getSpecification(), specQuery);
  }
}

