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

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

@Component
public class MapPatternMatchPredicateFactory {
  private final PatternMatchPredicateFactory patternMatchPredicateFactory = new PatternMatchPredicateFactory();

  public <T> Predicate<T> create(T query, Function<T, Map<String, String>> function) {
    List<Predicate<Entry<String, String>>> predicates = Optional
        .ofNullable(query)
        .map(function)
        .stream()
        .flatMap(m -> m.entrySet().stream())
        .map(ref -> patternMatchPredicateFactory.create(ref, Entry::getKey)
            .and(patternMatchPredicateFactory.create(ref, Entry::getValue)))
        .collect(toList());
    return e -> {
      Map<String, String> map = function.apply(e);
      return predicates
          .stream()
          .allMatch(map.entrySet().stream()::anyMatch);
    };
  }
}
