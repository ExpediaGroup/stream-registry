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

import java.time.Duration;

import org.apache.kafka.streams.integration.utils.EmbeddedKafkaCluster;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.expediagroup.streamplatform.streamregistry.app.StreamRegistryApp;

//TODO Full IT
@Ignore
public class StreamRegistryAppIT {

  @ClassRule
  public static EmbeddedKafkaCluster kafka = new EmbeddedKafkaCluster(1);
  @ClassRule
  public static SchemaRegistryJUnitRule schemaRegistry = new SchemaRegistryJUnitRule();

  @Test
  public void test() throws Exception {
    System.out.println("BOOTSTRAP_SERVER=" + kafka.bootstrapServers());
    System.out.println("SCHEMA_REGISTRY_URL=" + schemaRegistry.url());

    String[] args = new String[]{
        "--repository.kafka.bootstrap-servers=" + kafka.bootstrapServers(),
        "--schema.registry.url=" + schemaRegistry.url()
    };
    ConfigurableApplicationContext context = SpringApplication.run(new Class<?>[]{StreamRegistryApp.class}, args);
    Thread.sleep(Duration.ofHours(1).toMillis());
  }
}
