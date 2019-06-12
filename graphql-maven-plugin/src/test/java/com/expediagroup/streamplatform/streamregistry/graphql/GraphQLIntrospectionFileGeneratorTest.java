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
