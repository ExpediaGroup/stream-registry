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

import org.junit.Test;

public class PatternMatchPredicateFactoryTest {
  private final PatternMatchPredicateFactory underTest = new PatternMatchPredicateFactory();

  @Test
  public void patternMatch() {
    boolean result = underTest
        .create("^foo$", identity())
        .test("foo");
    assertThat(result, is(true));
  }

  @Test
  public void patternNoMatch() {
    boolean result = underTest
        .create("^foo$", identity())
        .test("bar");
    assertThat(result, is(false));
  }

  @Test
  public void nullPatternMatch() {
    boolean result = underTest
        .create(null, identity())
        .test("foo");
    assertThat(result, is(true));
  }

  @Test
  public void nullInput() {
    boolean result = underTest
        .create("^foo$", identity())
        .test(null);
    assertThat(result, is(false));
  }

  @Test
  public void nullPatternMatchAndInput() {
    boolean result = underTest
        .create(null, identity())
        .test(null);
    assertThat(result, is(true));
  }
}
