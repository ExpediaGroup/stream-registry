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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.model.Stream;

public class VersionPredicateFactoryTest {

  private final VersionPredicateFactory underTest = new VersionPredicateFactory();

  private java.util.stream.Stream<Stream> stream = java.util.stream.Stream.of(
      Stream.builder().version(1).build(),
      Stream.builder().version(2).build()
  );

  @Test
  public void filterSpecificVersion() {
    Stream query = Stream.builder().version(1).build();

    List<Stream> result = underTest.filter(query, stream).collect(toList());

    assertThat(result.size(), is(1));
    assertThat(result.get(0).getVersion(), is(1));
  }

  @Test
  public void filterLatestVersion() {
    Stream query = Stream.builder().version(0).build();

    List<Stream> result = underTest.filter(query, stream).collect(toList());

    assertThat(result.size(), is(1));
    assertThat(result.get(0).getVersion(), is(2));
  }

  @Test
  public void filterAllVersions() {
    Stream query = Stream.builder().version(null).build();

    List<Stream> result = underTest.filter(query, stream).collect(toList());

    assertThat(result.size(), is(2));
    assertThat(result.get(0).getVersion(), is(1));
    assertThat(result.get(1).getVersion(), is(2));
  }

  @Test(expected = IllegalArgumentException.class)
  public void negativeInputThrowsException() {
    Stream query = Stream.builder().version(-1).build();

    underTest.filter(query, stream).collect(toList());
  }
}
