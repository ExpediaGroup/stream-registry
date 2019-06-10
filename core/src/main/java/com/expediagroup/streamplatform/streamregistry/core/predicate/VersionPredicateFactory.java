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

import java.util.Comparator;

import com.google.common.collect.ImmutableListMultimap;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.model.Stream;

@Component
public class VersionPredicateFactory {
  public java.util.stream.Stream<Stream> filter(Stream query, java.util.stream.Stream<Stream> stream) {
    if (query.getVersion() == null) {
      return stream;
    } else if (query.getVersion() < 0) {
      throw new IllegalArgumentException("version must be greate than or equal to 0");
    } else if (query.getVersion() > 0) {
      return stream.filter(d -> query.getVersion().equals(d.getVersion()));
    }
    return stream
        .collect(ImmutableListMultimap.toImmutableListMultimap(
            s -> String.format("%s.%s", s.getDomain(), s.getName()),
            s -> s))
        .asMap()
        .values()
        .stream()
        .flatMap(c -> c
            .stream()
            .sorted(Comparator.comparing(Stream::getVersion).reversed())
            .limit(1));
  }
}
