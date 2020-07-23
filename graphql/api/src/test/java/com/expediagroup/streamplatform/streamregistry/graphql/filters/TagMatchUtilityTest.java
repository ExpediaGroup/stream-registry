/**
 * Copyright (C) 2018-2020 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.graphql.filters;

import static com.expediagroup.streamplatform.streamregistry.graphql.filters.TagMatchUtility.matchesTag;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.TagQuery;
import com.expediagroup.streamplatform.streamregistry.model.Tag;

public class TagMatchUtilityTest {

  @Test
  public void matchesTag_nameAndValue() {
    assertTrue(matchesTag(
        new Tag("name", "value"),
        TagQuery.builder().nameRegex("name").valueRegex("value").build()
    ));
  }

  @Test
  public void matchesTag_nameOnly() {
    assertTrue(matchesTag(
        new Tag("name", null),
        TagQuery.builder().nameRegex("name").build()
    ));
  }

  @Test
  public void matchesTag_valueDoesNotMatch() {
    assertFalse(matchesTag(
        new Tag("name", "value"),
        TagQuery.builder().nameRegex("name").valueRegex("x").build()
    ));
  }

  @Test
  public void matchesTag_nameDoesNotMatch() {
    assertFalse(matchesTag(
        new Tag("name", null),
        TagQuery.builder().nameRegex("x").build()
    ));
  }
}
