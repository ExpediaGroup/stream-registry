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

import com.expediagroup.streamplatform.streamregistry.graphql.client.StreamRegistryClient;

public class ITestClient extends StreamRegistryClient {

  public ITestClient(String url) {
    super(url);
  }

  public void createZone(ITestDataFactory factory) {
    invoke(factory.upsertZoneMutationBuilder().build());
  }

  public void createDomain(ITestDataFactory factory) {
    createZone(factory);
    upsertDomain(factory.upsertDomainMutationBuilder().build());
  }

  public void createSchema(ITestDataFactory factory) {
    createDomain(factory);
    upsertSchema(factory.upsertSchemaMutationBuilder().build());
  }

  public void createStream(ITestDataFactory factory) {
    createSchema(factory);
    upsertStream(factory.upsertStreamMutationBuilder().build());
  }

  public void createProducer(ITestDataFactory factory) {
    createStream(factory);
    upsertProducer(factory.upsertProducerMutationBuilder().build());
  }

  public void createConsumer(ITestDataFactory factory) {
    createStream(factory);
    upsertConsumer(factory.upsertConsumerMutationBuilder().build());
  }

}
