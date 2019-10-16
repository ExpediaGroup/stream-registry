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
package com.expediagroup.streamplatform.streamregistry.it.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;

import junit.framework.TestCase;

import com.apollographql.apollo.api.Mutation;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.FixedHostPortGenericContainer;

import com.expediagroup.streamplatform.streamregistry.StreamRegistryApp;

public abstract class ObjectIT {

  public ITestDataFactory factory = new ITestDataFactory(getClass().getSimpleName());

  private static final int POSTGRES_PORT = randomPort();

  @ClassRule
  public static FixedHostPortGenericContainer postgreSQLContainer =
      new FixedHostPortGenericContainer<>("postgres:latest")
          .withEnv("POSTGRES_USER", "streamregistry")
          .withEnv("POSTGRES_PASSWORD", "streamregistry")
          .withEnv("POSTGRES_DB", "streamregistry")
          .withFixedExposedPort(POSTGRES_PORT, 5432);

  private static ConfigurableApplicationContext context;
  private static String url;

  public static Client client;

  @BeforeClass
  public static void beforeClass() {
    String[] args = new String[]{
        "--server.port=0",
        "--repository.kafka.bootstrap-servers=x",
        "--repository.kafka.replicationFactor=1",
        "--schema.registry.url=http://schema-registry",
        "--spring.datasource.url=jdbc:postgresql://localhost:" + POSTGRES_PORT + "/streamregistry",
        "--spring.datasource.username=streamregistry",
        "--spring.datasource.password=streamregistry"
    };
    context = SpringApplication.run(StreamRegistryApp.class, args);
    url = "http://localhost:" + context.getEnvironment().getProperty("local.server.port") + "/graphql";

    client = new Client(url);
  }

  @Before
  public final void before() {
    createRequiredDatastoreState();
  }

  @AfterClass
  public static void afterClass() {
    if (context != null) {
      context.close();
      context = null;
    }
  }

  @Test
  public abstract void create();

  @Test
  public abstract void update();

  @Test
  public abstract void upsert();

  @Test
  public abstract void updateStatus();

  @Test
  public abstract void queryByKey();

  @Test
  public abstract void queryByRegex();

  public abstract void createRequiredDatastoreState();

  public Mutation assertMutationFails(Mutation mutation) {
    if (mutation.getClass().getSimpleName().toLowerCase().contains("insert")) {
      assertRequiresObjectIsAbsent(mutation);
    }
    if (mutation.getClass().getSimpleName().toLowerCase().contains("update")) {
      assertRequiresObjectIsPresent(mutation);
    }
    return mutation;
  }

  public void assertRequiresObjectIsAbsent(Mutation m) {
    try {
      client.invoke(m);
      TestCase.fail("Expected a ValidationException");
    } catch (RuntimeException e) {
      assertEquals("Can't create because it already exists", e.getMessage());
    }
  }

  public void setFactorySuffix(String suffix){
    factory=new ITestDataFactory(suffix);
    createRequiredDatastoreState();
  }

  public void assertRequiresObjectIsPresent(Mutation m) {
    try {
      client.invoke(m);
      TestCase.fail("Expected a ValidationException");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().startsWith("Can't update"));
    }
  }

  private static int randomPort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
