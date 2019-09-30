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

package com.expediagroup.streamplatform.streamregistry.app.queryFilters;

import static com.expediagroup.streamplatform.streamregistry.app.queryFilters.SpecificationMatchUtility.matchesSpecification;

import com.expediagroup.streamplatform.streamregistry.app.queries.ConsumerKeyQuery;
import com.expediagroup.streamplatform.streamregistry.app.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.core.filters.Filter;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;

public class ConsumerFilter implements Filter<Consumer> {

  private final ConsumerKeyQuery keyQuery;
  private final SpecificationQuery specQuery;

  public ConsumerFilter(ConsumerKeyQuery keyQuery, SpecificationQuery specQuery) {
    this.keyQuery = keyQuery;
    this.specQuery = specQuery;
  }

  public boolean matches(Consumer consumer) {
    if (keyQuery != null) {
      if (!consumer.getKey().getName().matches(keyQuery.getNameRegex())) {
        return false;
      }
      if (!consumer.getKey().getStreamDomain().matches(keyQuery.getStreamDomainRegex())) {
        return false;
      }
      if (!consumer.getKey().getStreamName().matches(keyQuery.getStreamNameRegex())) {
        return false;
      }
      if (!consumer.getKey().getZone().matches(keyQuery.getZoneRegex())) {
        return false;
      }
      if (keyQuery.getStreamVersion() != null && consumer.getKey().getStreamVersion() != keyQuery.getStreamVersion()) {
        return false;
      }
    }
    return matchesSpecification(consumer.getSpecification(), specQuery);
  }
}

