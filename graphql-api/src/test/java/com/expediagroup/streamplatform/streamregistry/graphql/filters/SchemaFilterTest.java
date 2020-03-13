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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SchemaKeyQuery.builder;

import java.util.Collections;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SchemaKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.TagQuery;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Tag;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;

public class SchemaFilterTest {

  private static final String REGEX = "match.*";
  private static final String MATCH = "match_this";
  private static final String FAIL = "fail_this";

  @Test
  public void filterBySchemaKey() {

    SchemaKeyQuery schemaKeyQuery = builder().domainRegex(REGEX).nameRegex(REGEX).build();
    SchemaFilter schemaFilter = new SchemaFilter(schemaKeyQuery, null);

    Schema schema = matchingSchema();
    assertTrue(schemaFilter.test(schema));

    schema = matchingSchema();
    schema.getKey().setDomain(FAIL);
    assertFalse(schemaFilter.test(schema));

    schema = matchingSchema();
    schema.getKey().setName(FAIL);
    assertFalse(schemaFilter.test(schema));
  }

  @Test
  public void filterBySpecificationQuery() {

    TagQuery tagQuery = TagQuery.builder().nameRegex(REGEX).valueRegex(REGEX).build();

    SpecificationQuery specificationQuery = SpecificationQuery.builder()
        .typeRegex(REGEX)
        .descriptionRegex(REGEX)
        .tags(Collections.singletonList(tagQuery))
        .build();

    SchemaFilter schemaFilter = new SchemaFilter(null, specificationQuery);

    Schema schema = matchingSchema();
    assertTrue(schemaFilter.test(schema));

    schema = matchingSchema();
    schema.getSpecification().setType(FAIL);
    assertFalse(schemaFilter.test(schema));

    schema = matchingSchema();
    schema.getSpecification().setDescription(FAIL);
    assertFalse(schemaFilter.test(schema));

    schema = matchingSchema();
    schema.getSpecification().getTags().get(0).setName(FAIL);
    assertFalse(schemaFilter.test(schema));

    schema = matchingSchema();
    schema.getSpecification().getTags().get(0).setValue(FAIL);
    assertFalse(schemaFilter.test(schema));

    schema = matchingSchema();
    schema.getSpecification().setType(null);
    assertFalse(schemaFilter.test(schema));

    schema = matchingSchema();
    schema.getSpecification().setDescription(null);
    assertFalse(schemaFilter.test(schema));

    schema = matchingSchema();
    schema.getSpecification().getTags().get(0).setName(null);
    assertFalse(schemaFilter.test(schema));

    schema = matchingSchema();
    schema.getSpecification().getTags().get(0).setValue(null);
    assertFalse(schemaFilter.test(schema));

    schema = matchingSchema();
    schema.getSpecification().setTags(null);
    assertFalse(schemaFilter.test(schema));
  }

  private Schema matchingSchema() {
    Schema schema = new Schema();
    schema.setKey(matchingSchemaKey());
    schema.setSpecification(matchingSpecification());
    return schema;
  }

  private SchemaKey matchingSchemaKey() {
    SchemaKey schemaKey = new SchemaKey();
    schemaKey.setDomain(MATCH);
    schemaKey.setName(MATCH);
    return schemaKey;
  }

  private Specification matchingSpecification() {
    Tag tag = new Tag();
    tag.setName(MATCH);
    tag.setValue(MATCH);

    Specification specification = new Specification();
    specification.setDescription(MATCH);
    specification.setTags(Collections.singletonList(tag));
    specification.setType(MATCH);
    return specification;
  }
}