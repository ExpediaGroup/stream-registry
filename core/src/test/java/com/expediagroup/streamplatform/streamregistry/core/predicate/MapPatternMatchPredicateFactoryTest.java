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

import static java.util.function.Function.identity;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class MapPatternMatchPredicateFactoryTest {
  private MapPatternMatchPredicateFactory underTest = new MapPatternMatchPredicateFactory();

  @Test
  public void mapPatternMatchKeyAndValue() {
    boolean result = underTest
        .create(Map.of("^foo$", "^bar$"), identity())
        .test(Map.of("foo", "bar"));
    assertThat(result, is(true));
  }

  @Test
  public void mapPatternMatchKeyOnly() {
    Map<String, String> query = new HashMap<>();
    query.put("^foo$", null);
    boolean result = underTest
        .create(query, identity())
        .test(Map.of("foo", "bar"));
    assertThat(result, is(true));
  }

  @Test
  public void mapPatternMatchValueOnly() {
    Map<String, String> query = new HashMap<>();
    query.put(null, "^bar$");
    boolean result = underTest
        .create(query, identity())
        .test(Map.of("foo", "bar"));
    assertThat(result, is(true));
  }
}
