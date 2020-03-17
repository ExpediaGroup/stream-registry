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
package com.expediagroup.streamplatform.streamregistry.it;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.SocketUtils;
import org.testcontainers.containers.GenericContainer;

import com.expediagroup.streamplatform.streamregistry.StreamRegistryApp;
import com.expediagroup.streamplatform.streamregistry.it.helpers.ITestClient;
import com.expediagroup.streamplatform.streamregistry.model.Domain;

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
  public static ITestClient client;
  private static ConfigurableApplicationContext context;

  @ClassRule
  public static GenericContainer postgres =
      new GenericContainer<>("postgres:12.0-alpine")
          .withLogConsumer(o -> log.info("Postgres: {}", o.getUtf8String().trim()))
          .withEnv("POSTGRES_USER", "streamregistry")
          .withEnv("POSTGRES_PASSWORD", "streamregistry")
          .withEnv("POSTGRES_DB", "streamregistry");

  @BeforeClass
  public static void before() {
    int port = SocketUtils.findAvailableTcpPort();

    log.info("Starting to run embedded spring app in port {}", port);

    /*
      When server.port=0, spring randomizes the port and it is not available until the context is fully initialized,
      then it can be retrieved with something like context.getEnvironment().getProperty("local.server.port")—that's
      the ideal case!—but in a slow resources environment (like a CI/CD docker) it may retrieve null, since environment
      is not ready yet.
    */
    String[] args = new String[] {
        String.format("--server.port=%d", port),
        "--spring.profiles.active=default,graphql,hibernate",
        "--spring.datasource.url=jdbc:postgresql://localhost:" + postgres.getMappedPort(5432) + "/streamregistry",
        "--spring.datasource.username=streamregistry",
        "--spring.datasource.password=streamregistry",
        "--spring.jpa.show-sql=false"
    };

    context = SpringApplication.run(StreamRegistryApp.class, args);

    final String url = String.format("http://localhost:%d/graphql", port);
    client = new ITestClient(url);
  }

  @AfterClass
  public static void after() {
    int domainCacheSize = CacheManager.ALL_CACHE_MANAGERS.get(0)
        .getCache(Domain.class.getName()).getSize();
    assertThat(domainCacheSize, greaterThan(0));

    if (context != null) {
      context.close();
      context = null;
    }
  }
}
