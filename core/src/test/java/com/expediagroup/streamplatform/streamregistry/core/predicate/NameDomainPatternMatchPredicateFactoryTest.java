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

import com.expediagroup.streamplatform.streamregistry.model.NameDomain;

public class NameDomainPatternMatchPredicateFactoryTest {
  private NameDomainPatternMatchPredicateFactory underTest = new NameDomainPatternMatchPredicateFactory();

  @Test
  public void nameDomainPatternMatch() {
    boolean result = underTest
        .create(NameDomain.builder().name("^foo$").domain("^bar$").build(), identity())
        .test(NameDomain.builder().name("foo").domain("bar").build());
    assertThat(result, is(true));
  }

  @Test
  public void nameDomainPatternMatchNameOnly() {
    boolean result = underTest
        .create(NameDomain.builder().name("^foo$").domain(null).build(), identity())
        .test(NameDomain.builder().name("foo").domain("bar").build());
    assertThat(result, is(true));
  }

  @Test
  public void nameDomainPatternMatchDomainOnly() {
    boolean result = underTest
        .create(NameDomain.builder().name(null).domain("^bar$").build(), identity())
        .test(NameDomain.builder().name("foo").domain("bar").build());
    assertThat(result, is(true));
  }
}
