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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class GraphQLIntrospectionFileGeneratorMojo extends AbstractMojo {
  private final GraphQLIntrospectionFileGenerator generator;
  @Parameter(required = true, property = "sourceSdlResource")
  @Setter(AccessLevel.PACKAGE)
  private String sourceSdlResource;
  @Parameter(required = true, property = "targetIntrospectionFile")
  @Setter(AccessLevel.PACKAGE)
  private String targetIntrospectionFile;

  public GraphQLIntrospectionFileGeneratorMojo() {
    this(new GraphQLIntrospectionFileGenerator());
  }

  @Override
  public void execute() {
    generator.generate(sourceSdlResource, targetIntrospectionFile);
  }
}
