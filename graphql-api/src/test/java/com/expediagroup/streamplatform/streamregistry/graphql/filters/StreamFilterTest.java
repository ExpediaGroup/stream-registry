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

import java.util.Collections;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SchemaKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.StreamKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.TagQuery;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.Tag;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;

public class StreamFilterTest {

  private static final String REGEX = "match.*";
  private static final String MATCH = "match_this";
  private static final String FAIL = "fail_this";

  private StreamFilter streamFilter;

  @Test
  public void filterBySpec() {

    TagQuery tagQuery = TagQuery.builder().nameRegex(REGEX).valueRegex(REGEX).build();

    SpecificationQuery specificationQuery = SpecificationQuery.builder()
        .typeRegex(REGEX)
        .descriptionRegex(REGEX)
        .tags(Collections.singletonList(tagQuery))
        .build();

    streamFilter = new StreamFilter(null, specificationQuery, null);

    Stream stream = new Stream();

    stream.setSpecification(matchingSpecification());
    assertTrue(streamFilter.test(stream));

    stream.setSpecification(matchingSpecification());
    stream.getSpecification().setType(FAIL);
    assertFalse(streamFilter.test(stream));

    stream.setSpecification(matchingSpecification());
    stream.getSpecification().setDescription(FAIL);
    assertFalse(streamFilter.test(stream));

    stream.setSpecification(matchingSpecification());
    stream.getSpecification().getTags().get(0).setName(FAIL);
    assertFalse(streamFilter.test(stream));

    stream.setSpecification(matchingSpecification());
    stream.getSpecification().getTags().get(0).setValue(FAIL);
    assertFalse(streamFilter.test(stream));

    //nulls

    stream.setSpecification(matchingSpecification());
    assertTrue(streamFilter.test(stream));

    stream.setSpecification(matchingSpecification());
    stream.getSpecification().setType(null);
    assertFalse(streamFilter.test(stream));

    stream.setSpecification(matchingSpecification());
    stream.getSpecification().setDescription(null);
    assertFalse(streamFilter.test(stream));

    stream.setSpecification(matchingSpecification());
    stream.getSpecification().getTags().get(0).setName(null);
    assertFalse(streamFilter.test(stream));

    stream.setSpecification(matchingSpecification());
    stream.getSpecification().getTags().get(0).setValue(null);
    assertFalse(streamFilter.test(stream));

    stream.setSpecification(matchingSpecification());
    stream.getSpecification().setTags(null);
    assertFalse(streamFilter.test(stream));
  }

  @Test
  public void filterBySchema() {
    SchemaKeyQuery schemaKeyQuery = SchemaKeyQuery.builder()
        .domainRegex(REGEX)
        .nameRegex(REGEX)
        .build();

    streamFilter = new StreamFilter(null, null, schemaKeyQuery);

    Stream stream = new Stream();

    stream.setSchemaKey(matchingSchemaKey());
    assertTrue(streamFilter.test(stream));

    stream.setSchemaKey(matchingSchemaKey());
    stream.getSchemaKey().setDomain(FAIL);
    assertFalse(streamFilter.test(stream));

    stream.setSchemaKey(matchingSchemaKey());
    stream.getSchemaKey().setName(FAIL);
    assertFalse(streamFilter.test(stream));

    //nulls....

    stream.setSchemaKey(matchingSchemaKey());
    stream.getSchemaKey().setName(null);
    assertFalse(streamFilter.test(stream));

    stream.setSchemaKey(matchingSchemaKey());
    stream.getSchemaKey().setDomain(null);
    assertFalse(streamFilter.test(stream));
  }

  @Test
  public void filterByStreamKey() {
    StreamKeyQuery streamKeyQuery = StreamKeyQuery.builder()
        .domainRegex(REGEX)
        .nameRegex(REGEX)
        .version(1)
        .build();

    streamFilter = new StreamFilter(streamKeyQuery, null, null);

    Stream stream = new Stream();

    stream.setKey(matchingStreamKey());
    assertTrue(streamFilter.test(stream));

    stream.setKey(matchingStreamKey());
    stream.getKey().setDomain(FAIL);
    assertFalse(streamFilter.test(stream));

    stream.setKey(matchingStreamKey());
    stream.getKey().setName(FAIL);
    assertFalse(streamFilter.test(stream));

    stream.setKey(matchingStreamKey());
    stream.getKey().setVersion(2);
    assertFalse(streamFilter.test(stream));

    stream.setKey(matchingStreamKey());
    stream.getKey().setVersion(null);
    assertFalse(streamFilter.test(stream));
  }

  private StreamKey matchingStreamKey() {
    StreamKey streamKey = new StreamKey();
    streamKey.setDomain(MATCH);
    streamKey.setName(MATCH);
    streamKey.setVersion(1);
    return streamKey;
  }

  private SchemaKey matchingSchemaKey() {
    SchemaKey schemaKey = new SchemaKey();
    schemaKey.setName(MATCH);
    schemaKey.setDomain(MATCH);
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