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

import static com.google.common.base.Charsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

public class GraphQLIntrospectionFileGenerator {
  private static void writeFile(String filename, String contents) {
    File file = new File(filename);
    try {
      Files.createParentDirs(file);
      Files.asCharSink(file, UTF_8).write(contents);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public void generate(String sourceSdlResource, String targetIntrospectionFile) {
    String schema = readResource(sourceSdlResource);
    TypeDefinitionRegistry registry = new SchemaParser().parse(schema);
    RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring().build();
    GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(registry, wiring);
    GraphQL graphql = GraphQL.newGraphQL(graphQLSchema).build();
    String introspectionQuery = readResource("introspection.query");
    ExecutionResult result = graphql.execute(introspectionQuery);
    String introspectionJson = serializeToJson(result);
    writeFile(targetIntrospectionFile, introspectionJson);
  }

  private String readResource(String resource) {
    try {
      return Resources.toString(Resources.getResource(resource), UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private String serializeToJson(ExecutionResult result) {
    try {
      return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(result);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
