package com.expediagroup.streamplatform.streamregistry.graphql;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GraphQLIntrospectionFileGeneratorMojoTest {
  @Mock
  private GraphQLIntrospectionFileGenerator generator;

  private GraphQLIntrospectionFileGeneratorMojo underTest;

  @Before
  public void before() {
    underTest = new GraphQLIntrospectionFileGeneratorMojo(generator);
  }

  @Test
  public void test() {
    underTest.setSourceSdlResource("sourceSdlResource");
    underTest.setTargetIntrospectionFile("targetIntrospectionFile");
    underTest.execute();

    verify(generator).generate("sourceSdlResource", "targetIntrospectionFile");
  }
}
