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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Predicates;

import org.springframework.stereotype.Component;

@Component
public class ObjectNodePatternMatchPredicateFactory {
  private static final ObjectNode emptyObjectNode = new ObjectMapper().createObjectNode();
  private final PatternMatchPredicateFactory patternMatchPredicateFactory = new PatternMatchPredicateFactory();

  public <T> Predicate<T> create(T query, Function<T, ObjectNode> function) {
    if (Optional
        .ofNullable(query)
        .map(function)
        .isPresent()) {
      throw new UnsupportedOperationException("Configuration is not currently queryable.");
    }
    return Predicates.alwaysTrue();
  }
}
