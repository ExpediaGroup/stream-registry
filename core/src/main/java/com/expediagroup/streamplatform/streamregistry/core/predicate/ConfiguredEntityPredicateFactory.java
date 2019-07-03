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
package com.expediagroup.streamplatform.streamregistry.core.predicate;

import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.model.ConfiguredEntity;

@Component
public class ConfiguredEntityPredicateFactory {
  private final EntityPredicateFactory entityPredicateFactory = new EntityPredicateFactory();
  private final PatternMatchPredicateFactory patternMatchPredicateFactory = new PatternMatchPredicateFactory();
  private final MapPatternMatchPredicateFactory mapPatternMatchPredicateFactory = new MapPatternMatchPredicateFactory();

  public <E extends ConfiguredEntity<?>> Predicate<E> create(E query) {
    return entityPredicateFactory.create(query)
        .and(patternMatchPredicateFactory.create(query, e -> Optional
            .ofNullable(e.getType())
            .orElse(null)))
        .and(mapPatternMatchPredicateFactory.create(query, e -> Optional
            .ofNullable(e.getConfiguration())
            .orElse(null)));
  }
}
