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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PatternMatchPredicateFactory {
  public <T> Predicate<T> create(T query, Function<T, String> function) {
    return Optional
        .ofNullable(query)
        .map(function)
        .map(Pattern::compile)
        .map(this::patternMatch)
        .map(predicate -> compose(predicate, function))
        .orElse(t -> true);
  }

  private <T> Predicate<T> compose(Predicate<String> predicate, Function<T, String> function) {
    return input -> predicate.test(function.apply(input));
  }

  private Predicate<String> patternMatch(Pattern pattern) {
    return input -> Optional.ofNullable(input)
        .map(pattern::matcher)
        .map(Matcher::matches)
        .orElse(false);
  }
}
