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

import junit.framework.TestCase;

import com.apollographql.apollo.api.Mutation;

import org.apache.kafka.streams.integration.utils.EmbeddedKafkaCluster;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.expediagroup.streamplatform.streamregistry.StreamRegistryApp;

public abstract class ObjectIT {

  public ITestDataFactory factory = new ITestDataFactory();

  @ClassRule
  public static EmbeddedKafkaCluster kafka = new EmbeddedKafkaCluster(1); //todo: remove
  @ClassRule
  public static SchemaRegistryJUnitRule schemaRegistry = new SchemaRegistryJUnitRule();

  private static ConfigurableApplicationContext context;
  private static String url;

  public static Client client;

  @BeforeClass
  public static void beforeClass() {
    String[] args = new String[] {
        "--server.port=0",
        "--repository.kafka.bootstrap-servers=" + kafka.bootstrapServers(),
        "--repository.kafka.replicationFactor=1",
        "--schema.registry.url=" + schemaRegistry.url()
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

  public void assertRequiresObjectIsPresent(Mutation m) {
    try {
      client.invoke(m);
      TestCase.fail("Expected a ValidationException");
    } catch (RuntimeException e) {
      assertEquals("Can't update because it doesn't exist", e.getMessage());
    }
  }
}
