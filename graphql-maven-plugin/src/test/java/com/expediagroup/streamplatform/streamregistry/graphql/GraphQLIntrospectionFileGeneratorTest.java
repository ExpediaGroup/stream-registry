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
package com.expediagroup.streamplatform.streamregistry.graphql;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class GraphQLIntrospectionFileGeneratorTest {
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Test
  public void test() throws Exception {
    GraphQLIntrospectionFileGenerator underTest = new GraphQLIntrospectionFileGenerator();
    File introspectionFile = temp.newFile("schema.json");
    underTest.generate("test.graphql", introspectionFile.getAbsolutePath());

    JsonNode jsonNode = new ObjectMapper().readTree(introspectionFile);

    JsonNode errors = jsonNode.get("errors");
    assertThat(errors.isArray(), is(true));
    assertThat(errors.size(), is(0));

    JsonNode data = jsonNode.get("data");
    assertThat(data.isObject(), is(true));
  }
}
