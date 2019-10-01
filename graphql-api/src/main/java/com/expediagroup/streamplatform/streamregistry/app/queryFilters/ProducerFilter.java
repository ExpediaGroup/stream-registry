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
package com.expediagroup.streamplatform.streamregistry.app.queryFilters;

import static com.expediagroup.streamplatform.streamregistry.app.queryFilters.SpecificationMatchUtility.matchesSpecification;
import static com.expediagroup.streamplatform.streamregistry.app.queryFilters.SpecificationMatchUtility.regex;

import com.expediagroup.streamplatform.streamregistry.app.queries.ProducerKeyQuery;
import com.expediagroup.streamplatform.streamregistry.app.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.core.services.Filter;
import com.expediagroup.streamplatform.streamregistry.model.Producer;

public class ProducerFilter implements Filter<Producer> {

  private final ProducerKeyQuery keyQuery;
  private final SpecificationQuery specQuery;

  public ProducerFilter(ProducerKeyQuery keyQuery, SpecificationQuery specQuery) {
    this.keyQuery = keyQuery;
    this.specQuery = specQuery;
  }

  public boolean matches(Producer d) {
    if (keyQuery != null) {
      if (!regex(d.getKey().getName(), keyQuery.getNameRegex())) {
        return false;
      }
      if (!regex(d.getKey().getStreamDomain(), keyQuery.getStreamDomainRegex())) {
        return false;
      }
      if (!regex(d.getKey().getStreamName(), keyQuery.getStreamNameRegex())) {
        return false;
      }
      if (!regex(d.getKey().getZone(), keyQuery.getZoneRegex())) {
        return false;
      }
      if (keyQuery.getStreamVersion() != null && d.getKey().getStreamVersion() != keyQuery.getStreamVersion()) {
        return false;
      }
    }
    return matchesSpecification(d.getSpecification(), specQuery);
  }
}
