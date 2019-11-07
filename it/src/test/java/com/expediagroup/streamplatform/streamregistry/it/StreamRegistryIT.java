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
package com.expediagroup.streamplatform.streamregistry.it;

import java.io.IOException;
import java.net.ServerSocket;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.FixedHostPortGenericContainer;

import com.expediagroup.streamplatform.streamregistry.StreamRegistryApp;
import com.expediagroup.streamplatform.streamregistry.it.helpers.Client;

@RunWith(Suite.class)
@SuiteClasses({
    DomainTestStage.class,
    SchemaTestStage.class,
    StreamTestStage.class,
    ProducerTestStage.class,
    ConsumerTestStage.class,
    ZoneTestStage.class,
    InfrastructureTestStage.class,
    StreamBindingTestStage.class,
    ProducerBindingTestStage.class,
    ConsumerBindingTestStage.class
})
@Slf4j
public class StreamRegistryIT {
  private static final int POSTGRES_PORT = randomPort();

  public static Client client;
  public static String url;

  private static ConfigurableApplicationContext context;

  @ClassRule
  public static FixedHostPortGenericContainer postgreSQLContainer =
      new FixedHostPortGenericContainer<>("postgres:12.0")
          .withLogConsumer(o -> log.info("Postgres: {}", o.getUtf8String().trim()))
          .withEnv("POSTGRES_USER", "streamregistry")
          .withEnv("POSTGRES_PASSWORD", "streamregistry")
          .withEnv("POSTGRES_DB", "streamregistry")
          .withFixedExposedPort(POSTGRES_PORT, 5432);

  @BeforeClass
  public static void before() {
    String[] args = new String[]{
        "--server.port=0",
        "--schema.registry.url=http://schema-registry",
        "--spring.datasource.url=jdbc:postgresql://localhost:" + POSTGRES_PORT + "/streamregistry",
        "--spring.datasource.username=streamregistry",
        "--spring.datasource.password=streamregistry",
        "--spring.jpa.hibernate.ddl-auto=create",
        "--spring.jpa.show-sql=false"
    };
    context = SpringApplication.run(StreamRegistryApp.class, args);
    url = "http://localhost:" + context.getEnvironment().getProperty("local.server.port") + "/graphql";

    client = new Client(url);
  }

  @AfterClass
  public static void after() {
    if (context != null) {
      context.close();
      context = null;
    }
  }

  @SneakyThrows(IOException.class)
  private static int randomPort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
  }
}
