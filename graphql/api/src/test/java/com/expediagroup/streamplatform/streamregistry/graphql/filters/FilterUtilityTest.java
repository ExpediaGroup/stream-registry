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

import static com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SchemaKeyQuery.builder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SchemaKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;

public class FilterUtilityTest {

  private static final String REGEX = "match.*";
  private static final String MATCH = "match_this";
  private static final String FAIL = "fail_this";

  @Test
  public void matches() {
    assertTrue(FilterUtility.matches(null, null));
    assertTrue(FilterUtility.matches(MATCH, null));
    assertTrue(FilterUtility.matches(MATCH, REGEX));
    assertFalse(FilterUtility.matches(FAIL, REGEX));
  }

  @Test
  public void matchesSchemaKey() {
    SchemaKeyQuery schemaKeyQuery = null;
    SchemaKey schemaKey = new SchemaKey();

    assertTrue(FilterUtility.matchesSchemaKey(schemaKey, schemaKeyQuery));

    schemaKeyQuery = builder().domainRegex(REGEX).nameRegex(REGEX).build();
    assertFalse(FilterUtility.matchesSchemaKey(schemaKey, schemaKeyQuery));

    schemaKey.setDomain(MATCH);
    schemaKey.setName(MATCH);
    assertTrue(FilterUtility.matchesSchemaKey(schemaKey, schemaKeyQuery));

    schemaKey.setDomain(MATCH);
    schemaKey.setName(FAIL);
    assertFalse(FilterUtility.matchesSchemaKey(schemaKey, schemaKeyQuery));

    schemaKey.setDomain(FAIL);
    schemaKey.setName(MATCH);
    assertFalse(FilterUtility.matchesSchemaKey(schemaKey, schemaKeyQuery));
  }

  @Test
  public void matchesSpecification() {

    Specification specification = new Specification();
    SpecificationQuery query = null;
    assertTrue(FilterUtility.matchesSpecification(specification, query));

    query = SpecificationQuery.builder().build();
    assertTrue(FilterUtility.matchesSpecification(specification, query));

    query = SpecificationQuery.builder().descriptionRegex(REGEX).build();
    assertFalse(FilterUtility.matchesSpecification(specification, query));

    query = SpecificationQuery.builder().typeRegex(REGEX).build();
    assertFalse(FilterUtility.matchesSpecification(specification, query));

    specification = new Specification(MATCH, null, FAIL, null);
    assertFalse(FilterUtility.matchesSpecification(specification, query));

    specification = new Specification(MATCH, null, MATCH, null);
    assertTrue(FilterUtility.matchesSpecification(specification, query));
  }
}