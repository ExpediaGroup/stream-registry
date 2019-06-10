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
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.model.NameDomain;

@Component
public class NameDomainPatternMatchPredicateFactory {
  private final PatternMatchPredicateFactory patternMatchPredicateFactory = new PatternMatchPredicateFactory();

  static <T> Function<T, String> ofNameDomain(
      Function<T, NameDomain> nameDomainFunction,
      Function<NameDomain, String> stringFunction) {
    return query -> Optional
        .ofNullable(query)
        .map(nameDomainFunction)
        .map(stringFunction)
        .orElse(null);
  }

  public <T> Predicate<T> create(T query, Function<T, NameDomain> function) {
    return patternMatchPredicateFactory.create(query, ofNameDomain(function, NameDomain::getName))
        .and(patternMatchPredicateFactory.create(query, ofNameDomain(function, NameDomain::getDomain)));
  }
}
